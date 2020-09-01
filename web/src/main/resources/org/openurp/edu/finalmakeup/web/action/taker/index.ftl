[#ftl]
[@b.head/]
[@b.toolbar title="毕业补考名单"/]
  [@edu.semester_bar name="semester.id" value=currentSemester/]
   <div class="search-container">
     <div class="search-panel">
      [@b.form name="searchForm" action="!search" title="ui.searchForm" target="listFrame" theme="search"]
      <input name="makeupTaker.makeupCourse.semester.id" value="${currentSemester.id}" type="hidden">
     [#include "searchForm.ftl"/]
     [/@]
     </div>
     <div class="search-list">
     [@b.div id="listFrame"/]
     </div>
    </div>
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
