/*
 * OpenURP, Agile University Resource Planning Solution.
 *
 * Copyright © 2014, The OpenURP Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openurp.edu.finalmakeup.web.action

import java.time.Instant

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.{ Numbers, Strings }
import org.beangle.data.dao.OqlBuilder
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.base.model.Department
import org.openurp.code.edu.model.{ CourseTakeType, ExamStatus, GradeType, GradingMode }
import org.openurp.code.service.CodeService
import org.openurp.edu.base.model._
import org.openurp.edu.base.service.SemesterService
import org.openurp.edu.base.web.ProjectSupport
import org.openurp.edu.exam.model.{ FinalMakeupCourse, FinalMakeupTaker }
import org.openurp.edu.finalmakeup.service.MakeupCourseSeqNoGenerator
import org.openurp.edu.finalmakeup.web.helper.{ MakeupMatrix, MakeupStat }
import org.openurp.edu.grade.course.model.{ CourseGrade, CourseGradeState, ExamGrade }
import org.openurp.edu.grade.course.service.CourseGradeCalculator
import org.openurp.edu.grade.model.Grade
import org.openurp.edu.graduation.audit.model.{ GraduateResult, GraduateSession }
import org.openurp.edu.graduation.plan.model.CourseAuditResult
import java.time.LocalDate

class CourseAction extends RestfulAction[FinalMakeupCourse] with ProjectSupport {
  var generator: MakeupCourseSeqNoGenerator = _
  var calcualtor: CourseGradeCalculator = _
  //  var semesterService: SemesterService = _
  var codeService: CodeService = _

  override def index: View = {
    put("departmentList", getDeparts)
    val query = OqlBuilder.from(classOf[GraduateSession], "session")
    query.where("session.project = :project", getProject)
    query.orderBy("session.graduateOn desc,session.name desc")
    val sessions = entityDao.search(query)
    put("sessions", sessions)
    val semester = getSemester(getProject, sessions.head.graduateOn)
    put("semester", semester)
    forward()
  }

  protected def getSemester(project: Project, date: LocalDate): Semester = {
    val builder = OqlBuilder.from(classOf[Semester], "semester")
      .where("semester.calendar in(:calendars)", project.calendars)
    builder.where(":date between semester.beginOn and  semester.endOn", date)
    builder.cacheable()
    val rs = entityDao.search(builder)
    if (rs.isEmpty) {
      val builder2 = OqlBuilder.from(classOf[Semester], "semester")
        .where("semester.calendar in(:calendars)", project.calendars)
      builder2.orderBy("abs(semester.beginOn - current_date() + semester.endOn - current_date())")
      builder2.cacheable()
      builder.limit(1, 2)
      val rs2 = entityDao.search(builder2)
      if (rs2.nonEmpty) {
        rs2.head
      } else {
        null
      }
    } else {
      rs.head
    }
  }

  protected override def getQueryBuilder: OqlBuilder[FinalMakeupCourse] = {
    val builder = super.getQueryBuilder
    get("squadName") foreach { squadName =>
      builder.where("exists(from makeupCourse.squads as squad where squad.name like :squadName)", "%" + squadName + "%")
    }
    builder
  }

  @ignore
  protected override def simpleEntityName: String = {
    "makeupCourse"
  }

  override def search: View = {
    val builder = getQueryBuilder
    this.addDepart(builder, "makeupCourse.depart")
    put("makeupCourses", entityDao.search(builder))
    forward()
  }

  def newCourseList: View = {
    val session = entityDao.get(classOf[GraduateSession], longId("session"))
    val semester = getSemester(getProject, session.graduateOn)
    put("semester", semester)
    val builder: OqlBuilder[Any] = OqlBuilder.from(classOf[CourseAuditResult].getName, "courseResult")
    builderMakeupQuery(builder, session, semester)
    builder.where("std2.state.department in (:department)", getDeparts)
    builder.join("left", "std2.state.squad", "squad")
    val fieldStr = "courseResult.course.id,std2.state.department.id,courseResult.course.code,courseResult.course.name,std2.state.department.name,squad.id,squad.code,squad.name"
    builder.groupBy(fieldStr)
    val orderBy = get("orderBy").orNull
    if (Strings.isBlank(orderBy)) builder.orderBy("count(distinct std2.id) desc,courseResult.course.code")
    else builder.orderBy(orderBy)
    val fieldStr2 = "courseResult.course.id,std2.state.department.id,courseResult.course.code,courseResult.course.name,std2.state.department.name,squad.id,squad.code,squad.name,count(distinct std2.id)"
    builder.select(fieldStr2)
    val stats = entityDao.search(builder).map(x => new MakeupStat(x.asInstanceOf[Array[AnyRef]]))
    put("statResults", stats)
    forward()
  }

  private def builderMakeupQuery[T](builder: OqlBuilder[T], session: GraduateSession, semester: Semester) = {
    builder.where("courseResult.passed=false")
    builder.where("courseResult.course.hasMakeup=true")
    // 按照有不及格的成绩来，不要采用courseResult.scores is not null
    builder.where("exists(from " + classOf[CourseGrade].getName + " cg where cg.std=std2 and cg.course=courseResult.course)")
    builder.where("std2.graduateOn=:graduateOn", session.graduateOn)
    builder.join("courseResult.groupResult.planResult.std", "std2")
    builder.where("exists(from " + classOf[GraduateResult].getName + " gr where gr.session.id=" + session.id + " and gr.std=std2)")
    val hql = new StringBuilder
    hql.append("not exists (")
    hql.append("  from ").append(classOf[FinalMakeupTaker].getName).append(" taker")
    hql.append(" where taker.makeupCourse.semester.id=" + semester.id)
    hql.append("   and taker.makeupCourse.course = courseResult.course")
    hql.append("   and taker.std = std2")
    hql.append(")")
    builder.where(hql.toString)
  }

  def addCourse: View = {
    val courseDepartClassIds = ids("makeupStat", classOf[String])
    val session = entityDao.get(classOf[GraduateSession], longId("session"))
    val semester = getSemester(getProject, session.graduateOn)
    put("semester", semester)
    for (courseDepartClassId <- courseDepartClassIds) {
      val idArray = Strings.split(courseDepartClassId, "-")
      val department = entityDao.get(classOf[Department], Numbers.toInt(idArray(0)))
      val course = entityDao.get(classOf[Course], Numbers.toLong(idArray(1)))
      var squad: Squad = null
      if (!("null" == idArray(2))) squad = entityDao.get(classOf[Squad], Numbers.toLong(idArray(2)))

      val builder = OqlBuilder.from(classOf[CourseAuditResult], "courseResult")
      builderMakeupQuery(builder, session, semester)
      builder.where("courseResult.course = :course", course)
      if (null == squad) builder.where("std2.state.squad is null", squad)
      else builder.where("std2.state.squad = :squad", squad)
      val results = entityDao.search(builder)
      val makeupCourse = getMakeupCourse(semester, department, squad, course)
      val proccessed = Collections.newSet[Student]
      for (result <- results) {
        val std = result.groupResult.planResult.std
        if (!proccessed.contains(std)) {
          var courseType = result.groupResult.courseType
          if (courseType.id < 0) courseType = result.groupResult.courseType
          if (null != courseType) {
            proccessed.add(std)
            val taker = new FinalMakeupTaker(makeupCourse, std, result.groupResult.courseType)
            taker.scores = result.scores
            taker.remark = result.remark
            makeupCourse.takers += taker
            makeupCourse.stdCount = makeupCourse.takers.size
          }
        }
      }
      entityDao.saveOrUpdate(makeupCourse)
    }
    redirect("newCourseList", "info.save.success")
  }

  private def getMakeupCourse(semester: Semester, department: Department, squad: Squad, course: Course): FinalMakeupCourse = {
    val builder = OqlBuilder.from(classOf[FinalMakeupCourse], "makeupCourse")
    builder.where("makeupCourse.semester = :semester", semester)
    builder.where("makeupCourse.course = :course", course)
    if (null == squad) builder.where("size(makeupCourse.squads)=0")
    else builder.where(":squad in elements(makeupCourse.squads)", squad)
    val makeupCourses = entityDao.search(builder)
    if (Collections.isEmpty(makeupCourses)) {
      val makeupCourse = new FinalMakeupCourse
      makeupCourse.semester = semester
      makeupCourse.course = course
      makeupCourse.depart = department
      if (null != squad) makeupCourse.squads += squad
      generator.genSeqNo(makeupCourse)
      entityDao.saveOrUpdate(makeupCourse)
      makeupCourse
    } else {
      makeupCourses.head
    }
  }

  def merge(): View = {
    val removeds = entityDao.find(classOf[FinalMakeupCourse], longIds("makeupCourse")).toBuffer
    val target = removeds.head
    removeds -= target
    for (makeupCourse <- removeds) {
      target.mergeWith(makeupCourse)
    }
    entityDao.remove(removeds)
    entityDao.saveOrUpdate(target)
    redirect("search", "info.save.success")
  }

  def split(): View = {
    val makeupCourseId = longId("makeupCourse")
    val makeupCourse = entityDao.get(classOf[FinalMakeupCourse], makeupCourseId)
    if (Collections.isNotEmpty(makeupCourse.squads)) {
      for (squad <- makeupCourse.squads) {
        val newMc = new FinalMakeupCourse()
        newMc.squads += squad
        newMc.course = makeupCourse.course
        newMc.depart = makeupCourse.depart
        newMc.semester = makeupCourse.semester
        for (taker <- makeupCourse.takers) {
          if (squad.id.equals(taker.std.state.map(_.squad.map(_.id)).getOrElse(0))) {
            newMc.takers += new FinalMakeupTaker(newMc, taker.std, taker.courseType)
          }
        }
        newMc.stdCount = newMc.takers.size
        generator.genSeqNo(newMc)
        entityDao.saveOrUpdate(newMc)
      }
    }
    entityDao.remove(makeupCourse)
    redirect("search", "info.save.success")
  }

  def squadStat(): View = {
    val session = entityDao.get(classOf[GraduateSession], longId("session"))
    val semester = getSemester(getProject, session.graduateOn)
    put("semester", semester)
    val departs = getDeparts
    put("departments", departs)
    val builder: OqlBuilder[Array[Any]] = OqlBuilder.from(classOf[FinalMakeupTaker].getName, "mt")
    builder.where("mt.makeupCourse.semester=:semester", semester)
    val depart = getInt("department.id") match {
      case None => departs.head
      case Some(d) => entityDao.get(classOf[Department], d)
    }
    put("department", depart)
    builder.where("mt.makeupCourse.depart  = :depart", depart)
    builder.where("mt.std.state.squad is not null")
    builder.groupBy("mt.std.state.squad.id,mt.makeupCourse.course.id")
    builder.select("mt.std.state.squad.id,mt.makeupCourse.course.id,count(*)")
    val stats = entityDao.search(builder)
    val matrix = new MakeupMatrix
    for (data <- stats) {
      val squad = entityDao.get(classOf[Squad], data(0).asInstanceOf[Number].longValue)
      val course = entityDao.get(classOf[Course], data(1).asInstanceOf[Number].longValue)
      matrix.add(squad, course, data(2).asInstanceOf[Number].intValue)
    }
    matrix.build()
    put("matrix", matrix)
    forward()
  }

  def editTeacher: View = {
    val removeds = entityDao.find(classOf[FinalMakeupCourse], longIds("makeupCourse"))
    val departTeacherMap = Collections.newMap[Department, Seq[Teacher]]
    for (makeupCourse <- removeds) {
      departTeacherMap.get(makeupCourse.depart) match {
        case None =>
          departTeacherMap.put(makeupCourse.depart, entityDao.findBy(classOf[Teacher], "user.department", List(makeupCourse.depart)))
        case _ =>
      }
    }
    put("departTeacherMap", departTeacherMap)
    put("makeupCourses", removeds)
    forward()
  }

  def designationTeacher: View = {
    val makeupCourses = entityDao.find(classOf[FinalMakeupCourse], longIds("makeupCourse"))
    for (makeupCourse <- makeupCourses) {
      getLong(makeupCourse.id.toString) foreach { teacherId =>
        makeupCourse.teacher = entityDao.find(classOf[Teacher], teacherId)
      }
    }
    entityDao.saveOrUpdate(makeupCourses)
    redirect("editTeacher", "info.save.success", "&makeupCourseIds=" + get("makeupCourseIds"))
  }

  def takers(): View = {
    put("makeupCourse", entityDao.get(classOf[FinalMakeupCourse], longId("makeupCourseId")))
    forward()
  }

  def gradeTable(): View = {
    put("makeupCourses", entityDao.find(classOf[FinalMakeupCourse], longIds("makeupCourse")))
    forward()
  }

  def printGrade(): View = {
    val a = longIds("makeupCourse")
    val builder = OqlBuilder.from(classOf[FinalMakeupCourse], "makeupCourse")
    builder.where("makeupCourse.id in (:makeupCourseIds)", a)
    builder.where("makeupCourse.confirmed = true")
    val makeupCourses = entityDao.search(builder)
    put("makeupCourses", makeupCourses)
    val gradeMap = Collections.newMap[FinalMakeupCourse, Seq[CourseGrade]]
    for (makeupCourse <- makeupCourses) {
      val builder2 = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
      builder2.where("courseGrade.crn = :crn", makeupCourse.crn)
      builder2.where("courseGrade.course = :course", makeupCourse.course)
      builder2.where("courseGrade.semester = :semester", makeupCourse.semester)
      gradeMap.put(makeupCourse, entityDao.search(builder2))
    }
    put("gradeMap", gradeMap)
    put("gradeTypes", List(entityDao.get(classOf[GradeType], GradeType.Makeup)))
    val MAKEUP = entityDao.get(classOf[GradeType], GradeType.Makeup)
    val MAKEUP_GA = entityDao.get(classOf[GradeType], GradeType.MakeupGa)
    put("MAKEUP", MAKEUP)
    put("MAKEUP_GA", MAKEUP_GA)
    forward()
  }

  def input(): View = {
    val makeupCourse = entityDao.get(classOf[FinalMakeupCourse], longId("makeupCourse"))
    put("makeupCourse", makeupCourse)
    if (makeupCourse.confirmed) {
      return redirect("printGrade", "&makeupCourseIds=" + makeupCourse.id, "info.save.success")
    }
    put("gradeMap", getCourseGradeMap(makeupCourse))
    put("gradeTypes", List(entityDao.get(classOf[GradeType], GradeType.Makeup)))
    val examStatuses = codeService.get(classOf[ExamStatus]).toBuffer
    val removed = Collections.newBuffer[ExamStatus]
    for (es <- examStatuses) {
      if (es.deferred) removed += es
    }
    examStatuses --= removed
    put("examStatuses", examStatuses)
    put("NormalExamStatus", codeService.get(classOf[ExamStatus], ExamStatus.Normal))
    forward()
  }

  private def getCourseGradeMap(makeupCourse: FinalMakeupCourse): Map[Student, CourseGrade] = {
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    builder.where("courseGrade.crn = :crn", makeupCourse.crn)
    builder.where("courseGrade.course = :course", makeupCourse.course)
    builder.where("courseGrade.semester = :semester", makeupCourse.semester)
    builder.where("courseGrade.clazz is null")
    val grades = entityDao.search(builder)
    grades.map(x => (x.std, x)).toMap
  }

  override def save(): View = {
    val makeupCourse = entityDao.get(classOf[FinalMakeupCourse], longId("makeupCourse"))
    val markStyle = entityDao.get(classOf[GradingMode], GradingMode.Percent)
    val gradeMap = getCourseGradeMap(makeupCourse)
    val grades = Collections.newBuffer[CourseGrade]
    val status = if (getBoolean("justSave", true)) Grade.Status.New
    else Grade.Status.Confirmed

    val MAKEUP = entityDao.get(classOf[GradeType], GradeType.Makeup)
    val state = new CourseGradeState
    for (taker <- makeupCourse.takers) {
      val score = getFloat(MAKEUP.id + "_" + taker.std.id)
      val examStatusInputId = "examStatus_" + MAKEUP.id + "_" + taker.std.id
      val examStatusId = getInt(examStatusInputId, 0)
      if (null != score || examStatusId != ExamStatus.Normal) {
        var grade = gradeMap.getOrElse(taker.std, null)
        if (null == grade) grade = newCourseGrade(taker, markStyle)
        grade.status = status
        var examGrade = grade.getExamGrade(MAKEUP).orNull
        if (null == examGrade) {
          examGrade = new ExamGrade()
          examGrade.gradeType = MAKEUP
          examGrade.examStatus = new ExamStatus(examStatusId)
          examGrade.score = score
          examGrade.gradingMode = grade.gradingMode
          grade.addExamGrade(examGrade)
        }
        examGrade.status = status
        calcualtor.updateScore(examGrade, score, grade.gradingMode)
        calcualtor.calcAll(grade, state)
        grades += grade
      }
    }
    if (Grade.Status.Confirmed == status) {
      makeupCourse.confirmed = true
      makeupCourse.published = false
      entityDao.saveOrUpdate(grades, makeupCourse)
      redirect("printGrade", "info.save.success", "&makeupCourseIds=" + makeupCourse.id)
    } else {
      entityDao.saveOrUpdate(grades)
      redirect("input", "info.save.success", "&makeupCourseId=" + makeupCourse.id)
    }
  }

  private def newCourseGrade(taker: FinalMakeupTaker, gradingMode: GradingMode): CourseGrade = {
    val grade = new CourseGrade()
    grade.std = taker.std
    grade.project = taker.std.project
    grade.course = taker.makeupCourse.course
    grade.semester = taker.makeupCourse.semester
    grade.crn = Some(taker.makeupCourse.crn)
    grade.courseTakeType = new CourseTakeType()
    grade.courseTakeType.id = CourseTakeType.Normal
    grade.courseType = taker.courseType
    grade.gradingMode = gradingMode
    grade.status = 0
    //    grade.createdAt=Instant.now
    grade.updatedAt = Instant.now
    grade
  }

  def editPublished(): View = {
    val makeupCourseIds = longIds("makeupCourse")
    val published = getBoolean("published", true)
    if (null != makeupCourseIds) {
      val makeupCourses = entityDao.find(classOf[FinalMakeupCourse], makeupCourseIds)
      val MAKEUP_GA = entityDao.get(classOf[GradeType], GradeType.MakeupGa)
      val grades = Collections.newBuffer[CourseGrade]
      val state = new CourseGradeState
      val status = if (published) Grade.Status.Published else Grade.Status.New
      for (makeupCourse <- makeupCourses) {
        val gradeMap = getCourseGradeMap(makeupCourse)
        for (taker <- makeupCourse.takers) {
          gradeMap.get(taker.std) foreach { grade =>
            grade.status = status
            grade.getGaGrade(MAKEUP_GA) foreach { gaGrade =>
              gaGrade.status = status
            }
            calcualtor.calcAll(grade, state)
            grades += grade
          }
        }
        if (!published) makeupCourse.confirmed = false
        makeupCourse.published = published
      }
      entityDao.saveOrUpdate(grades, makeupCourses)
    }
    redirect("search", "info.save.success")
  }
}
