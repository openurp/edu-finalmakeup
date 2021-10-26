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

package org.openurp.edu.finalmakeup.web.helper
import org.beangle.commons.bean.orderings.MultiPropertyOrdering
import org.beangle.commons.collection.Collections
import org.openurp.base.edu.model.{Course, Squad}

class MakeupMatrix {
  val datas = Collections.newMap[Squad, collection.mutable.Map[Course, Int]]
  val courseStat = Collections.newMap[Course, Int]
  val squadStat = Collections.newMap[Squad, Int]
  var courses: collection.Seq[Course] = _
  var squads: collection.Seq[Squad] = _

  def build(): Unit = {
    squads = squadStat.keys.toBuffer.sorted(new MultiPropertyOrdering("department,name"))
    courses = courseStat.keys.toBuffer.sorted(new Ordering[Course]() {
      override def compare(first: Course, second: Course): Int = courseStat(second) - courseStat(first)
    })
  }

  def add(squad: Squad, course: Course, count: Int): Unit = {
    var cs = datas.getOrElseUpdate(squad, Collections.newMap[Course, Int])
    squadStat.put(squad, squadStat.getOrElse(squad, 0) + count)
    courseStat.put(course, courseStat.getOrElse(course, 0) + count)
    cs.put(course, cs.getOrElse(course, 0) + count)
  }

  def get(squad: Squad, course: Course): Int = {
    if (null != squad && null != course) {
      datas.get(squad) match {
        case None => 0
        case Some(cs) => cs.getOrElse(course, 0)
      }
    } else if (null == squad && null == course) {
      courseStat.values.sum
    } else if (null == squad) courseStat(course)
    else squadStat(squad)
  }
}
