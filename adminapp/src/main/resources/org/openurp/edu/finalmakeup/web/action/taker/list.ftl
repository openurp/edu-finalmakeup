[#ftl]
[@b.head/]
  [@b.grid items=makeupTakers var="makeupTaker"]
    [@b.gridbar]
      bar.addItem("添加", action.method("addSetting"));
      bar.addItem("删除", action.multi('removeTaker',"确定删除？"));
      bar.addItem("${b.text('action.export')}", "exportData()");
      function exportData(){
        var form = document.searchForm;
        bg.form.addInput(form, "keys", "std.user.code,std.user.name,std.state.grade,std.state.campus.name,std.level.name,std.state.department.name,std.state.major.name,std.state.direction.name,std.state.squad.name,makeupCourse.crn,makeupCourse.course.code,makeupCourse.course.name,scores,remark");
        bg.form.addInput(form, "titles", "学号,姓名,年级,校区,培养层次,院系,专业,方向,班级,课程序号,课程代码,课程名称,最好成绩,备注");
        bg.form.addInput(form, "fileName", "毕业补考名单");
        bg.form.submit(form, "${b.url('!export')}","_self");
      }
    [/@]
    [@b.row]
      [@b.boxcol/]
      [@b.col title="学号" property="std.user.code" width="13%"/]
      [@b.col title="姓名" property="std.user.name" width="10%"/]
      [@b.col title="课程序号" property="makeupCourse.crn" width="6%"/]
      [@b.col title="课程代码" width="6%" property="makeupCourse.course.code"/]
      [@b.col title="课程名称" width="20%" property="makeupCourse.course.name"/]
      [@b.col title="院系" width="10%" property="std.state.department.name"/]
      [@b.col title="班级" property="std.state.squad.name"/]
      [@b.col title="最高分数" property="scores" width="6%"/]
      [@b.col title="备注" property="remark" width="6%"/]
    [/@]
  [/@]
[@b.foot/]
