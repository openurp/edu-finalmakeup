[#ftl][@b.head/]
<body>
<table id="taskBar"></table>
    <#assign task=lastMakeupTask>
    <#assign takes=lastMakeupTask.takes?sort_by(["std","code"])>
    <br>
     <table width="100%" align="center">
     <tr>
      <td align="center" colspan="4">
       <B><@i18nName systemConfig.school/>毕业补考名单</B>
       <br>
       学年学期：${task.semester.schoolYear}学年 ${task.semester.name}学期
      </td>
     </tr>
     <tr class="infoTitle">
       <td  ><@text name="attr.taskNo"/>：${task.crn}&nbsp;&nbsp;</td>
       <td  ><@text name="attr.courseNo"/>：${task.course.code}&nbsp;&nbsp;</td>
       <td  ><@text name="attr.courseName"/>：<@i18nName task.course?if_exists/>&nbsp;&nbsp;</td>
       <td  >人数:${task.stdCount}</td>
     </tr>
   </table>
   <table class="listTable"  width="100%">
     <tr class="darkColumn" align="center">
       <td width="5%">序号</td>
       <td width="12%"><@text name="attr.stdNo"/></td>
       <td width="10%"><@text name="attr.personName"/></td>
       <td width="5%">性别</td>
       <td width="24%">专业</td>
       <td width="22%">班级</td>
       <td width="17%">课程类别</td>
     </tr>
     <#list takes as take>
     <tr align="center" >
       <td>${take_index+1}</td>
       <td>${take.std.code}</td>
       <td><@i18nName take.std/></td>
       <td><@i18nName take.std.gender/></td>
       <td><@i18nName take.std.major!/></td>
       <td><@i18nName take.std.majorClass!/></td>
       <td><@i18nName take.courseType!/></td>
     </tr>
     </#list>
     </table>
 <script>
  var bar=new ToolBar("taskBar","毕业补考课程学生名单(${task.stdCount}人)",null,true,true);
  bar.addPrint();
  bar.addBack();
 </script>
 </body>
[@b.foot/]
