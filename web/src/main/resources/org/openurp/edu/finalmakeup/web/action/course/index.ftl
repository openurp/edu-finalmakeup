[#ftl]
[@b.head/]
  [@b.toolbar title="毕业补考成绩管理"]
  [/@]

<div style="background: url('${b.base}/static/images/semesterBarBg.png') repeat-x scroll 50% 50% #DEEDF7;border: 1px solid #AED0EA;color: #222222;font-weight: bold;height:28px;">
  [@b.form name="indexForm" action="!index" target="main" theme="html"]
  <div style="margin-left:4px;margin-top:2px;float:left;line-height:16px;height:16px;">
    <label for="indexForm.batch.id">审核批次:</label>
    [#if batches?size == 0]
      <select id="indexForm.batch.id" name="batch.id" style="width:200px;">
        <option>缺少毕业审核批次</option>
      </select>
    [#else]
      [#assign selectedId = Parameters['batch.id']!batches[0].id?string /]
      <select id="indexForm.batch.id" name="batch.id" style="width:200px;" onchange="bg.form.submit(this.form)">
        [#list batches as batch]
        <option value="${batch.id}"[#if batch.id?string == selectedId] selected[/#if]>${batch.name}</option>
        [/#list]
      </select>
    [/#if]
  </div>
  [@b.toolbar title=""]
    [#if batches?size > 0]
    bar.addItem("补考统计", function() {
      bg.form.submit(document.indexForm, "${b.url("!squadStat")}", "_blank");
    });
    bar.addItem("补考名单", function() {
      bg.form.submit(document.indexForm, "${b.url("taker")}", "_blank");
    });
    bar.addItem("生成补考课程", function() {
      bg.form.submit(document.indexForm, "${b.url("!newCourseList")}", "_blank");
    });
    [/#if]
  [/@]
  [/@]
</div>

  <div class="search-container">
      <div class="search-panel" >
        [@b.form name="makeupCourseIndexForm" action="!search" title="ui.searchForm" target="makeupCourses" theme="search"]
          [#include "searchForm.ftl"/]
          <input type="hidden" name="makeupCourse.semester.id" value="${semester.id}">
        [/@]
      </div>
      <div class="search-list">[@b.div id="makeupCourses" href="!search?makeupCourse.semester.id="+semester.id/]</div>
  </div>
[@b.foot/]
