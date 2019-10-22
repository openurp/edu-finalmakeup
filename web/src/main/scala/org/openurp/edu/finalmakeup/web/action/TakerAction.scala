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

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.edu.base.model.{Semester, Student}
import org.openurp.edu.base.web.ProjectSupport
import org.openurp.edu.exam.model.{FinalMakeupCourse, FinalMakeupTaker}

class TakerAction extends RestfulAction[FinalMakeupTaker] with ProjectSupport {

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

  def removeTaker: View = {
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
    query.where("task.semester.id=:semesterId",semesterId.get)
    query.orderBy("task.course.name,task.crn")
    put("tasks",entityDao.search(query));
    forward()
  }

  def addTakers(): View = {
    val semesterId = getInt("semester.id")
    val query = OqlBuilder.from(classOf[FinalMakeupCourse], "task")
    getInt("semester.id").foreach(semesterId => {
      query.where("task.semester.id=:semesterId", semesterId)
    })
    get("makeupCourse.crn").foreach(seqNo => {
      query.where("task.crn=:crn", seqNo)
    })
    val tasks = entityDao.search(query)
    var stdCode = get("stdCodes").orNull
    stdCode = Strings.replace(stdCode, " ", ",")
    val stdCodes = Strings.split(stdCode, ",")
    val stds = entityDao.findBy(classOf[Student], "user.code", stdCodes.toList)
    if (stds.isEmpty) {
      redirect("search", "&makeupTaker.makeupCourse.semester.id=" + semesterId.get, "不存在该学号学生")
    } else {
      stds.foreach(std => {
        val task = tasks.head
        val courseType = task.course.courseType
        val take = new FinalMakeupTaker(task, std, courseType)
        take.scores="--"
        task.takers += take
        task.stdCount = task.stdCount + 1
      })
      entityDao.saveOrUpdate(tasks)
      redirect("search", "&makeupTaker.makeupCourse.semester.id=" + semesterId.get, "info.save.success")
    }
  }
}
