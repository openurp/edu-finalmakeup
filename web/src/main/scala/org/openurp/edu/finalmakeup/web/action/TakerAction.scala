/*
 * Copyright (C) 2005, The OpenURP Software.
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
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.web.action.annotation.ignore
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.action.RestfulAction
import org.openurp.base.edu.model.{Course, Semester, Student}
import org.openurp.starter.edu.helper.ProjectSupport
import org.openurp.edu.exam.model.{FinalMakeupCourse, FinalMakeupTaker}
import org.openurp.edu.finalmakeup.service.MakeupCourseService

class TakerAction extends RestfulAction[FinalMakeupTaker] with ProjectSupport {
  var makeupCourseService: MakeupCourseService = _

  @ignore
  protected override def simpleEntityName: String = {
    "makeupTaker"
  }

  override def indexSetting(): Unit = {
    val semesterId = getInt("semester.id")
    val semester = {
      semesterId match {
        case None => getCurrentSemester
        case _ => entityDao.get(classOf[Semester], semesterId.get)
      }
    }
    put("currentSemester", semester)
    put("project",getProject)
    super.indexSetting()
  }

  def stat: View = {
    val semesterId = getLong("semester.id")
    val query: OqlBuilder[Array[Any]] = OqlBuilder.from(classOf[FinalMakeupTaker].getName, "t")
    query.where("take.makeupCourse.semester.id=:semesterId", semesterId)
    query.groupBy("t.std.id")
    query.select("t.std.id,count(t.id)")
    val rs = entityDao.search(query)
    val stats = Collections.newMap[Int, collection.mutable.Set[Long]]
    for (data <- rs) {
      val cnt = data(1).asInstanceOf[Number].intValue()
      val stdId = data(0).asInstanceOf[Long]
      val stdIds = stats.getOrElseUpdate(cnt, Collections.newSet)
      stdIds += stdId
    }
    put("stats", stats)
    forward()
  }

  def removeTaker(): View = {
    val tasks = Collections.newSet[FinalMakeupCourse]
    longIds("makeupTaker") foreach { takerId =>
      val taker = entityDao.get(classOf[FinalMakeupTaker], takerId)
      val task = taker.makeupCourse
      tasks += task
      task.stdCount = task.stdCount - 1
      task.takers -= taker
    }
    entityDao.saveOrUpdate(tasks)
    redirect("search", "info.remove.success")
  }

  def addSetting(): View = {
    val semesterId = getInt("makeupTaker.makeupCourse.semester.id")
    val query = OqlBuilder.from(classOf[FinalMakeupCourse], "task")
    query.where("task.semester.id=:semesterId", semesterId.get)
    query.orderBy("task.course.name,task.crn")
    put("tasks", entityDao.search(query))
    forward()
  }

  def addTakers(): View = {
    val semester = entityDao.get(classOf[Semester], intId("semester"))
    val courseCode = get("courseCode")
    val crn = get("makeupCourse.crn", "")
    var stdCode = get("stdCodes").orNull
    stdCode = Strings.replace(stdCode, " ", ",")
    val stdCodes = Strings.split(stdCode, ",")
    val stds = entityDao.findBy(classOf[Student], "user.code", stdCodes.toList)

    if (stds.isEmpty) {
      redirect("search", "&makeupTaker.makeupCourse.semester.id=" + semester.id, "不存在该学号学生")
    } else {
      var result = "info.save.success"
      if (Strings.isNotBlank(crn)) {
        val query = OqlBuilder.from(classOf[FinalMakeupCourse], "mc").where("mc.crn=:crn", crn)
        query.where("mc.semester=:semester", semester)
        val makeupCourse = entityDao.search(query).head
        val rs = Collections.newBuffer[String]
        stds foreach { std =>
          val msg = makeupCourseService.addTaker(makeupCourse, std)
          if (Strings.isNotBlank(msg)) {
            rs += msg
          }
        }
        result = rs.mkString(",")
      } else {
        val query = OqlBuilder.from(classOf[Course], "c").where("c.code=:code", courseCode.get)
        val courses = entityDao.search(query)
        if (courses.isEmpty) {
          return redirect("search", "课程代码" + courseCode.get + "不存在")
        }
        val course = courses.head
        val rs = Collections.newBuffer[String]
        stds foreach { std =>
          val msg = makeupCourseService.addTaker(semester, course, std)
          if (Strings.isNotBlank(msg)) {
            rs += msg
          }
        }
        result = rs.mkString(",")
      }
      if (Strings.isBlank(result)) result = "info.save.success"
      redirect("search", result)
    }
  }
}
