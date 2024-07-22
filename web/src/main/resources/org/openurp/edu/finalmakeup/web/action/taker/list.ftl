[#ftl]
[@b.head/]
  [@b.grid items=makeupTakers var="makeupTaker"]
    [@b.gridbar]
      bar.addItem("添加到..", action.method("addSetting"));
      bar.addItem("删除", action.multi('removeTaker',"确定删除？"));
      bar.addItem("${b.text('action.export')}", "exportData()");
      function exportData(){
        var form = document.searchForm;
        bg.form.addInput(form, "keys", "std.code,std.name,std.state.grade,std.state.campus.name,std.level.name,std.state.department.name,std.state.major.name,std.state.direction.name,std.state.squad.name,makeupCourse.crn,makeupCourse.course.code,makeupCourse.course.name,scores,remark");
        bg.form.addInput(form, "titles", "学号,姓名,年级,校区,培养层次,院系,专业,方向,班级,课程序号,课程代码,课程名称,最好成绩,备注");
        bg.form.addInput(form, "fileName", "毕业补考名单");
        bg.form.submit(form, "${b.url('!exportData')}","_self");
      }
    [/@]
    [@b.row]
      [@b.boxcol/]
      [@b.col title="学号" property="std.code" width="13%"/]
      [@b.col title="姓名" property="std.name" width="8%"/]
      [@b.col title="课程序号" property="makeupCourse.crn" width="6%"/]
      [@b.col title="课程代码" width="8%" property="makeupCourse.course.code"/]
      [@b.col title="课程名称" width="20%" property="makeupCourse.course.name"/]
      [@b.col title="院系" property="std.state.department.name"]
        ${(makeupTaker.std.state.department.shortName)!makeupTaker.std.state.department.name}
      [/@]
      [@b.col title="班级" width="17%" property="std.state.squad.name"]
      <span style="font-size:0.8em">${(makeupTaker.std.state.squad.name)!}</span>
      [/@]
      [@b.col title="备注" property="remark" width="18%"]
      <span style="font-size:0.8em">
      ${makeupTaker.remark!}
      </span>
      [/@]
    [/@]
  [/@]
[@b.foot/]
