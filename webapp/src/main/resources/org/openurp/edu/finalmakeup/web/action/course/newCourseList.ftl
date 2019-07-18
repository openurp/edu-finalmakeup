[#ftl]
[@b.head/]
  [@b.toolbar title="毕业补考补考课程列表(" + statResults?size + "门次)"]
    bar.addClose();
  [/@]
  [@b.grid items=statResults var="makeupStat" sortable="false"]
    [@b.gridbar]
      bar.addItem("选择课程，生成补考记录", action.multi("addCourse"));
    [/@]
    [@b.row]
      [@b.boxcol /]
      [@b.col title="序号" width="7%"]${makeupStat_index+1}[/@]
      [@b.col title="课程代码" property="courseResult.course.code" width="10%"]${makeupStat.courseCode}[/@]
      [@b.col title="课程名称" property="courseResult.course.name"]${makeupStat.courseName}[/@]
      [@b.col title="部门" property="std2.state.department.name"]${makeupStat.stdDepartmentName}[/@]
      [@b.col title="班级" property="std2.state.squad.name"]${(makeupStat.stdSquadName)!"--"}[/@]
      [@b.col title="人数" width="7%" property="count(distinct std2.id)"]${makeupStat.stdCount}[/@]
    [/@]
  [/@]
[@b.foot/]
