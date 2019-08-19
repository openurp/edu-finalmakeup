[#ftl]
[@b.head/]
[@b.toolbar title="添加毕业补考学生列表"]
  bar.addItem("添加", "add(document.actionForm)");
  bar.addBack();
   function add(form){
    form.action = "addTakes";
    bg.form.submit(form);
  }
[/@]
<form name="actionForm" action="takerr!addTakes.action" method="post">
     <input type="hidden" name="semester.id" value="${Parameters['makeupTaker.makeupCourse.semester.id']}"/>
     <table class="formTable" width="80%" align="center">
      <tr>
       <td class="infoTitle">
       课程序号:<input name="makeupCourse.crn" type="text" value="" style="width:60px" maxlength="32"/>
      </td>
      </tr>
      <tr>
       <td class="infoTitle">学号(多个以单个空格 或者半角逗号结束,):
       <input type="text" name="stdCodes" value="" style="width:300px" maxlength="100"/></td></tr>
    </table>
</form>
[@b.foot/]
