[#ftl]
[@b.textfields names="makeupCourse.crn;课程序号,makeupCourse.course.code;课程代码,makeupCourse.course.name;课程名称"/]
[@b.select label="部门" name="makeupCourse.depart.id" items=departmentList?sort_by(["code"]) empty="..."/]
[@b.select label="成绩提交" name="makeupCourse.confirmed" items={ "1": "已提交", "0": "未提交" } empty="..."/]
[@b.select label="成绩发布" name="makeupCourse.published" items={ "1": "已发布", "0": "未发布" } empty="..."/]
[@b.textfields names="squadName;班级名称"/]
<tr><td align="center"><input type="reset" value="重置">&nbsp;&nbsp;<input type="submit" value="查询" onclick="bg.form.submit('makeupCourseIndexForm');return false;"></td></tr>
