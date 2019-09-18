[#ftl]
[@b.head/]
[@b.toolbar title="毕业补考名单"/]
  [@edu_base.semester_bar name="semester.id" value=currentSemester/]
   <table width="100%"  class="indexpanel" height="89%">
    <tr>
     <td valign="top"  style="width:180px" class="index_view">
      [@b.form name="searchForm" action="!search" title="ui.searchForm" target="listFrame" theme="search"]
      <input name="makeupTaker.makeupCourse.semester.id" value="${currentSemester.id}" type="hidden">
     [#include "searchForm.ftl"/]
     [/@]
     </td>
     <td valign="top">
     [@b.div id="listFrame"/]
     </td>
    </tr>
  <table>
 <script>
  var form = document.searchForm;
  search();
  function search(pageNo,pageSize,orderBy){
    form.target="listFrame";
    form.action="${b.url('!search')}";
    bg.form.submit(form)
  }
  document.semesterForm.method="GET";
 </script>
[@b.foot/]
