[#ftl]
[@b.textfields names="makeupCourse.crn;课程序号,makeupCourse.course.code;课程代码,makeupCourse.course.name;课程名称"/]
[@b.select label="部门" name="makeupCourse.depart.id" items=departmentList?sort_by(["code"]) empty="..."/]
[@b.select label="成绩状态" name="makeupCourse.status" items={ "0":"未提交","1": "已提交", "2": "已发布" } empty="..."/]
[@b.textfields names="squadName;班级名称"/]
