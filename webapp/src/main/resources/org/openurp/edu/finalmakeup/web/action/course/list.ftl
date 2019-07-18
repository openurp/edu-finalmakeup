[#ftl]
[@b.head/]
  [@b.grid items=makeupCourses var="makeupCourse"]
    [@b.gridbar]
      bar.addItem("指定阅卷教师", action.multi("editTeacher"));
      var printMenu = bar.addMenu("成绩发布", action.multi("editPublished","确定发布?","&published=1"));
      printMenu.addItem("取消发布", action.multi("editPublished","确定取消发布?","&published=0"));
      bar.addItem("合并班级", action.multi("merge"));
      [#--bar.addItem("拆分班级",action.single("split"));
      --]
      bar.addItem("成绩录入", "inputTask()");
      bar.addItem('成绩登分册', action.multi("gradeTable",null,null,"_blank"));
      bar.addItem("成绩打印", action.multi("printGrade",null,null,"_blank"));
      bar.addItem("删除", action.remove());
      function inputTask(){
         var form = document.makeupCourseForm;
         var clazzId = bg.input.getCheckBoxValues("makeupCourse.id");
         if(clazzId=="" || clazzId.indexOf(",")>0){
            alert("请仅选择一个教学任务.");
            return false;
         }
         bg.form.addInput(form,"makeupCourseId",clazzId);
         bg.form.submit(form,"${b.url('!input')}","_blank");
   }
    [/@]
    [@b.row]
      [@b.boxcol/]
      [@b.col title="课程序号" property="crn" width="10%"/]
      [@b.col title="课程代码" property="course.code" width="10%"/]
      [@b.col title="课程名称" property="course.name"/]
      [@b.col title="部门" property="depart.name"/]
      [@b.col title="所在班级" width="20%"]
        [#list makeupCourse.squads as squad]${squad.name}[#if squad_has_next]&nbsp;[/#if][/#list]
      [/@]
      [@b.col title="阅卷教师" property="teacher.name" width="10%"/]
      [@b.col title="人数" property="stdCount" width="4%"/]
      [@b.col title="提交" property="confirmed" width="6%"]${makeupCourse.confirmed?string("是", "否")}[/@]
      [@b.col title="发布" property="published" width="6%"]${makeupCourse.published?string("是", "否")}[/@]

    [/@]
  [/@]
  [@b.form name="makeupCourseForm" action="!index"/]
[@b.foot/]
