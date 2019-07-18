[#ftl]
[@b.head/]
 <table id="taskBar"></table>
 [@b.toolbar title="毕业补考课程列表"]
  bar.addItem("指定阅卷教师", 'updateteacher(document.defaultFormitem)');

   //指定阅卷教师
   function updateteacher(form){
    var makeupCourseIds = bg.input.getCheckBoxValues("makeupCourseId");
    if(""==makeupCourseIds){alert("请选择一个或多个");return;}
    form.action="course!designationTeacher.action?makeupCourseIds="+makeupCourseIds;
    form.target="_self";
    form.submit();
   }
[/@]
  <form action="course!designationTeacher.action" name="defaultFormitem" method="post" onsubmit="return false;">
    <center>
     <table class="gridtable" width="100%" sortable="true" id="listTable" style="font-size:12px">
     <tr align="center" class="gridhead" >
      <td class="select">
       <input type="checkBox" name="makeupCourseIds" class="box" checked>
      </td>
      <td width="7%" text="课程序号" id="makeupCourse.crn" class="tableHeaderSort">课程序号</td>
      <td width="10%" text="课程代码" id="makeupCourse.course.code" class="tableHeaderSort">课程代码</td>
      <td width="15%" text="课程名称" id="makeupCourse.course.name" class="tableHeaderSort">课程名称</td>
      <td width="15%" text="部门" id="makeupCourse.depart.name" class="tableHeaderSort">部门</td>
      <td width="30%" text="所在班级" id="makeupCourse.adminClass.name" class="tableHeaderSort">所在班级</td>
      <td width="10%"text="阅卷教师" id="makeupCourse.teacher.name" class="tableHeaderSort">阅卷教师</td>
      <td width="10%"text="人数" id="makeupCourse.stdCount" class="tableHeaderSort">人数</td>
    </tr>
    <tbody>
    [#list makeupCourses as task]
    <tr class="brightStyle" align="center">
    <td class="select">
     <input type="checkbox" class="box" name="makeupCourseId" checked value="${task.id?if_exists}">
    </td>
    <td>${task.crn}</td>
      <td>${task.course.code}</td>
      <td>${task.course.name}</td>
      <td>${task.depart.name}</td>
      <td>
         [#list task.squads as admin]
             ${admin.name!}[#if admin_has_next],[/#if]
         [/#list]
       </td>
       <td>
    <select name="${task.id}" id="${task.id}" style="width:80%">
     <option value="0">请选择</option>
    [#if departTeacherMap.get(task.depart)??]
      [#list departTeacherMap.get(task.depart) as teacher]
      <option value="${teacher.id}"  [#if (task.teacher.id)?if_exists?string==teacher.id?string]selected[/#if]>${teacher.user.name}</option>
      [/#list]
      [/#if]
    </select>
       </td>
      <td>${task.stdCount}</td>
    </tr>
  [/#list]
    </tbody>
   </table>
  </center>
    </form>

  <br><br><br><br>
  <form name="actionForm" method="post" onsubmit="return false;">
     <input type="hidden" name="makeupCourseIds" value=""/>
  </form>
[@b.foot/]
