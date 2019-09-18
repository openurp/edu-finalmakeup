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
package org.openurp.edu.finalmakeup.web

import org.beangle.cdi.bind.BindModule
import org.openurp.code.service.impl.CodeServiceImpl
import org.openurp.edu.finalmakeup.service.impl.MakeupCourseSeqNoGeneratorImpl
import org.openurp.edu.finalmakeup.web.action.{CourseAction, TakerAction}
import org.openurp.edu.grade.course.service.GradeRateService
import org.openurp.edu.grade.course.service.impl.{DefaultCourseGradeCalculator, GradeRateServiceImpl}
import org.openurp.edu.grade.setting.service.impl.CourseGradeSettingsImpl

class DefaultModule extends BindModule {

  protected override def binding(): Unit = {
    bind(classOf[CourseAction], classOf[TakerAction])
    bind(classOf[CodeServiceImpl])
    bind(classOf[MakeupCourseSeqNoGeneratorImpl])
  }
}
