/*
 * Copyright (C) 2014, The OpenURP Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openurp.edu.finalmakeup.web.action

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.{Numbers, Strings}
import org.beangle.data.dao.OqlBuilder
import org.beangle.security.Securities
import org.beangle.web.action.annotation.ignore
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.action.RestfulAction
import org.openurp.base.edu.model.*
import org.openurp.base.hr.model.Teacher
import org.openurp.base.model.{Department, Project, Semester}
import org.openurp.base.std.model.{Squad, Student}
import org.openurp.code.edu.model.{CourseTakeType, ExamStatus, GradeType, GradingMode}
import org.openurp.code.service.CodeService
import org.openurp.edu.exam.model.{FinalMakeupCourse, FinalMakeupTaker}
import org.openurp.edu.finalmakeup.service.MakeupCourseService
import org.openurp.edu.finalmakeup.web.helper.{MakeupMatrix, MakeupStat}
import org.openurp.edu.grade.model.*
import org.openurp.edu.grade.service.CourseGradeCalculator
import org.openurp.starter.web.support.ProjectSupport
import org.openurp.std.graduation.model.{GraduateBatch, GraduateResult}

import java.time.{Instant, LocalDate}

class CourseAction extends RestfulAction[FinalMakeupCourse] with ProjectSupport {
  var makeupCourseService: MakeupCourseService = _
  var calculator: CourseGradeCalculator = _

  override def index(): View = {
    given project: Project = getProject

    put("departmentList", getDeparts)
    val query = OqlBuilder.from(classOf[GraduateBatch], "batch")
    query.where("batch.project = :project", project)
    query.orderBy("batch.graduateOn desc,batch.name desc")
    val batches = entityDao.search(query)
    put("batches", batches)
    val batch = getInt("batch.id") match {
      case None => batches.head
      case Some(sid) => batches.find(_.id == sid).getOrElse(batches.head)
    }
    val semester = getSemester(project, batch.graduateOn)
    put("semester", semester)
    forward()
  }

  override def search(): View = {
    val builder = getQueryBuilder
    queryByDepart(builder, "makeupCourse.depart")
    put("makeupCourses", entityDao.search(builder))
    forward()
  }

  protected override def getQueryBuilder: OqlBuilder[FinalMakeupCourse] = {
    val builder = super.getQueryBuilder
    get("squadName") foreach { squadName =>
      if (Strings.isNotBlank(squadName)) {
        builder.where("exists(from makeupCourse.squads as squad where squad.name like :squadName)", "%" + squadName + "%")
      }
    }
    builder
  }

  def newCourseList(): View = {
    given project: Project = getProject

    val batch = entityDao.get(classOf[GraduateBatch], getLongId("batch"))
    val semester = getSemester(project, batch.graduateOn)
    put("semester", semester)

    val builder: OqlBuilder[Any] = OqlBuilder.from(classOf[AuditCourseResult].getName, "courseResult")
    builderMakeupQuery(builder, batch, semester)
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

  protected def getSemester(project: Project, date: LocalDate): Semester = {
    val builder = OqlBuilder.from(classOf[Semester], "semester")
      .where("semester.calendar=:calendar", project.calendar)
    builder.where(":date between semester.beginOn and  semester.endOn", date)
    builder.cacheable()
    val rs = entityDao.search(builder)
    if (rs.isEmpty) {
      val builder2 = OqlBuilder.from(classOf[Semester], "semester")
        .where("semester.calendar=:calendar", project.calendar)
      val dataStr = date.toString
      builder2.orderBy(s"abs(semester.beginOn - to_date('$dataStr','yyyy-MM-dd') + semester.endOn - to_date('$dataStr','yyyy-MM-dd'))")
      builder2.cacheable()
      builder2.limit(1, 2)
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

  private def builderMakeupQuery[T](builder: OqlBuilder[T], batch: GraduateBatch, semester: Semester) :Unit= {
    builder.where("courseResult.passed=false")
    builder.where("courseResult.course.hasMakeup=true")
    // 按照有不及格的成绩来，不要采用courseResult.scores is not null
    builder.where("exists(from " + classOf[CourseGrade].getName + " cg where cg.std=std2 and cg.course=courseResult.course)")
    builder.where("std2.graduateOn =:graduateOn", batch.graduateOn)
    builder.join("courseResult.groupResult.planResult.std", "std2")
    builder.where("exists(from " + classOf[GraduateResult].getName + " gr where gr.batch.id=" + batch.id + " and gr.std=std2)")
    val hql = new StringBuilder
    hql.append("not exists (")
    hql.append("  from ").append(classOf[FinalMakeupTaker].getName).append(" taker")
    hql.append(" where taker.makeupCourse.semester.id=" + semester.id)
    hql.append("   and taker.makeupCourse.course = courseResult.course")
    hql.append("   and taker.std = std2")
    hql.append(")")
    builder.where(hql.toString)
  }

  def addCourse(): View = {
    val courseDepartClassIds = getIds("makeupStat", classOf[String])
    val batch = entityDao.get(classOf[GraduateBatch], getLongId("batch"))
    val semester = getSemester(getProject, batch.graduateOn)
    put("semester", semester)
    for (courseDepartClassId <- courseDepartClassIds) {
      val idArray = Strings.split(courseDepartClassId, "-")
      val department = entityDao.get(classOf[Department], Numbers.toInt(idArray(0)))
      val course = entityDao.get(classOf[Course], Numbers.toLong(idArray(1)))
      var squad: Squad = null
      if (!("null" == idArray(2))) squad = entityDao.get(classOf[Squad], Numbers.toLong(idArray(2)))

      val builder = OqlBuilder.from(classOf[AuditCourseResult], "courseResult")
      builderMakeupQuery(builder, batch, semester)
      builder.where("courseResult.course = :course", course)
      if (null == squad) builder.where("std2.state.squad is null", squad)
      else builder.where("std2.state.squad = :squad", squad)
      val results = entityDao.search(builder)
      val makeupCourse = makeupCourseService.getOrCreate(semester, course, department, Option(squad))
      val proccessed = Collections.newSet[Student]
      for (result <- results) {
        val std = result.groupResult.planResult.std
        if (!proccessed.contains(std)) {
          var courseType = result.groupResult.courseType
          if (courseType.id < 0) courseType = result.groupResult.courseType
          if (null != courseType) {
            proccessed.add(std)
            val taker = new FinalMakeupTaker(makeupCourse, std, result.groupResult.courseType)
            result.scores match {
              case null => taker.scores = "--"
              case e => taker.scores = e
            }
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

  def merge(): View = {
    val removeds = entityDao.find(classOf[FinalMakeupCourse], getLongIds("makeupCourse")).toBuffer
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
    val makeupCourseId = getLongId("makeupCourse")
    val makeupCourse = entityDao.get(classOf[FinalMakeupCourse], makeupCourseId)
    makeupCourseService.split(makeupCourse)
    redirect("search", "info.save.success")
  }

  def squadStat(): View = {
    given project: Project = getProject

    val batch = entityDao.get(classOf[GraduateBatch], getLongId("batch"))
    val semester = getSemester(getProject, batch.graduateOn)
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
    val removeds = entityDao.find(classOf[FinalMakeupCourse], getLongIds("makeupCourse"))
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

  def designationTeacher(): View = {
    val makeupCourses = entityDao.find(classOf[FinalMakeupCourse], getLongIds("makeupCourse"))
    for (makeupCourse <- makeupCourses) {
      getLong(makeupCourse.id.toString) foreach { teacherId =>
        makeupCourse.teacher = entityDao.find(classOf[Teacher], teacherId)
      }
    }
    entityDao.saveOrUpdate(makeupCourses)
    redirect("editTeacher", "&makeupCourseIds=" + get("makeupCourseIds"), "info.save.success")
  }

  def takers(): View = {
    put("makeupCourse", entityDao.get(classOf[FinalMakeupCourse], getLongId("makeupCourseId")))
    forward()
  }

  def gradeTable(): View = {
    put("makeupCourses", entityDao.find(classOf[FinalMakeupCourse], getLongIds("makeupCourse")))
    forward()
  }

  def printGrade(): View = {
    val a = getLongIds("makeupCourse")
    val builder = OqlBuilder.from(classOf[FinalMakeupCourse], "makeupCourse")
    builder.where("makeupCourse.id in (:makeupCourseIds)", a)
    builder.where("makeupCourse.status > 0")
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
    val makeupCourse = entityDao.get(classOf[FinalMakeupCourse], getLongId("makeupCourse"))
    put("makeupCourse", makeupCourse)
    if (makeupCourse.status > 0) {
      return redirect("printGrade", "&makeupCourseIds=" + makeupCourse.id, "info.save.success")
    }
    put("gradeMap", getCourseGradeMap(makeupCourse))
    put("gradeTypes", List(entityDao.get(classOf[GradeType], GradeType.Makeup)))
    val examStatuses = codeService.get(classOf[ExamStatus]).toBuffer
    val removed = Collections.newBuffer[ExamStatus]
    for (es <- examStatuses) {
      if (es.hasDeferred) removed += es
    }
    examStatuses --= removed
    put("examStatuses", examStatuses)
    put("NormalExamStatus", codeService.get(classOf[ExamStatus], ExamStatus.Normal))
    forward()
  }

  override def save(): View = {
    val makeupCourse = entityDao.get(classOf[FinalMakeupCourse], getLongId("makeupCourse"))
    val markStyle = entityDao.get(classOf[GradingMode], GradingMode.Percent)
    val gradeMap = getCourseGradeMap(makeupCourse)
    val grades = Collections.newBuffer[CourseGrade]
    val status = if (getBoolean("justSave", true)) Grade.Status.New
    else Grade.Status.Confirmed

    val MAKEUP = entityDao.get(classOf[GradeType], GradeType.Makeup)
    val state = new CourseGradeState
    for (taker <- makeupCourse.takers) {
      val score = getFloat(s"${MAKEUP.id}_${taker.std.id}")
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
          examGrade.createdAt = Instant.now
          examGrade.updatedAt = Instant.now
          examGrade.gradingMode = grade.gradingMode
          grade.addExamGrade(examGrade)
        }
        examGrade.status = status
        calculator.updateScore(examGrade, score, grade.gradingMode)
        calculator.calcAll(grade, state)
        grades += grade
      }
    }
    if (Grade.Status.Confirmed == status) {
      makeupCourse.status = Grade.Status.Confirmed
      entityDao.saveOrUpdate(grades, makeupCourse)
      redirect("printGrade", "&makeupCourseIds=" + makeupCourse.id, "info.save.success")
    } else {
      entityDao.saveOrUpdate(grades)
      redirect("input", "&makeupCourseId=" + makeupCourse.id, "info.save.success")
    }
  }

  private def newCourseGrade(taker: FinalMakeupTaker, gradingMode: GradingMode): CourseGrade = {
    val grade = new CourseGrade()
    grade.std = taker.std
    grade.project = taker.std.project
    grade.course = taker.makeupCourse.course
    grade.semester = taker.makeupCourse.semester
    grade.crn = taker.makeupCourse.crn
    grade.courseTakeType = new CourseTakeType()
    grade.courseTakeType.id = CourseTakeType.Normal
    grade.courseType = taker.courseType
    grade.gradingMode = gradingMode
    grade.status = 0
    grade.examMode = taker.makeupCourse.course.examMode
    grade.updatedAt = Instant.now
    grade.createdAt = Instant.now
    grade.operator = Some(Securities.user)
    grade
  }

  def editPublished(): View = {
    val makeupCourseIds = getLongIds("makeupCourse")
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
            calculator.calcAll(grade, state)
            grades += grade
          }
        }
        makeupCourse.status = status
      }
      entityDao.saveOrUpdate(grades, makeupCourses)
    }
    redirect("search", "info.save.success")
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

  @ignore
  protected override def simpleEntityName: String = {
    "makeupCourse"
  }
}
