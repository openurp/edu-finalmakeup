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

package org.openurp.edu.finalmakeup.web.helper

/**
 * 补考课程班级统计
 *
 * @author duant
 */
class MakeupStat(val datas: Array[AnyRef]) {
  var courseId: Long = 0L
  var departmentId: Int = 0
  var courseCode: String = _
  var courseName: String = _
  var stdDepartmentName: String = _
  var stdSquadId: Option[Long] = None
  var stdSquadCode: Option[String] = None
  var stdSquadName: Option[String] = None
  var stdCount = 0

  courseId = datas(0).asInstanceOf[Long]
  departmentId = datas(1).asInstanceOf[Number].intValue
  courseCode = datas(2).asInstanceOf[String]
  courseName = datas(3).asInstanceOf[String]
  stdDepartmentName = datas(4).asInstanceOf[String]
  if (null != datas(5)) {
    stdSquadId = Some(datas(5).asInstanceOf[Number].longValue())
    stdSquadCode = Some(datas(6).asInstanceOf[String])
    stdSquadName = Some(datas(7).asInstanceOf[String])
  }
  stdCount = datas(8).asInstanceOf[Number].intValue

  def id: String = {
    s"${departmentId}-${courseId}-${stdSquadId.getOrElse("null")}"
  }

}
