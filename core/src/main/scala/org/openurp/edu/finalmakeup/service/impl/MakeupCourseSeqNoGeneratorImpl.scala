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
package org.openurp.edu.finalmakeup.service.impl

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.{Numbers, Strings}
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.openurp.edu.base.model.Semester
import org.openurp.edu.exam.model.FinalMakeupCourse
import org.openurp.edu.finalmakeup.service.MakeupCourseSeqNoGenerator

import scala.collection.mutable

object MakeupCourseSeqNoGeneratorImpl {
  val initSeqNo = "0001"
  val prefix = "BK"
}

class MakeupCourseSeqNoGeneratorImpl extends MakeupCourseSeqNoGenerator {
  var entityDao: EntityDao = _

  def genSeqNo(makeupCourse: FinalMakeupCourse): Unit = {
    if (!Strings.isEmpty(makeupCourse.crn)) return
    this synchronized {
      val seqNos = loadSeqNo(makeupCourse.semester)
      var newSeqNo = 0
      var breaked = false
      for (s <- seqNos if !breaked) {
        val seqNo = s.substring(MakeupCourseSeqNoGeneratorImpl.prefix.length)
        if (!seqNo.matches(".*[^\\d]+.*")) {
          if (Numbers.toInt(seqNo) - newSeqNo >= 2) {
            breaked = true
          } else {
            newSeqNo = Numbers.toInt(seqNo)
          }
        }
      }
      newSeqNo += 1
      putSeqNo(makeupCourse, newSeqNo)
    }
  }
  private def putSeqNo(makeupCourse: FinalMakeupCourse, seqNo: Int): Unit = {
    makeupCourse.crn =  "BK" + Strings.repeat("0", 4 - String.valueOf(seqNo).length) + seqNo
  }

  private def loadSeqNo(semester: Semester): Seq[String] = {
    val builder = OqlBuilder.from(classOf[FinalMakeupCourse].getName + " makeupCourse")
    builder.where("makeupCourse.semester = :semester", semester)
    builder.orderBy("makeupCourse.crn")
    builder.select("makeupCourse.crn")
    entityDao.search(builder)
  }

  def genSeqNos(makeupCourses: collection.Seq[FinalMakeupCourse]): Unit = {
    val courseBySemesterMap = Collections.newMap[Semester, mutable.Buffer[FinalMakeupCourse]]
    for (makeupCourse <- makeupCourses) {
      if (Strings.isEmpty(makeupCourse.crn)) {
        courseBySemesterMap.getOrElseUpdate(makeupCourse.semester, Collections.newBuffer[FinalMakeupCourse]) += makeupCourse
      }
    }
    for (semester <- courseBySemesterMap.keySet) {
      genCourseSeqNos(semester, courseBySemesterMap(semester))
    }
  }

  private def genCourseSeqNos(semester: Semester, courses: collection.Seq[FinalMakeupCourse]): Unit = {
    if (courses.isEmpty) return
    this synchronized {
      val allSeqNos = loadSeqNo(semester)
      var newSeqNo = 0
      var seq = 0
      var allocated = 0
      val courseIter = courses.iterator
      var breaked = false
      for (s <- allSeqNos if !breaked) {
        val seqNo = s.substring(MakeupCourseSeqNoGeneratorImpl.prefix.length)
        seq = Numbers.toInt(seqNo)
        if (seq - newSeqNo >= 2) {
          val gap = seq - newSeqNo - 1
          var i = 0
          while (i < gap) {
            allocated += 1
            newSeqNo += 1
            putSeqNo(courseIter.next, newSeqNo)
            if (allocated >= courses.size) {
              i = gap //break
            } else {
              i += 1
            }
          }

          if (allocated >= courses.size) {
            breaked = true
          } else {
            newSeqNo = seq
          }
        }
      }
      while (allocated < courses.size) {
        newSeqNo += 1
        allocated += 1
        putSeqNo(courseIter.next, newSeqNo)
      }
    }
  }

}
