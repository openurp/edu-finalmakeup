[#ftl]
[@b.head/]
  [@b.toolbar title="毕业补考成绩管理"]
  [/@]

<div style="background: url('${base}/static/images/semesterBarBg.png') repeat-x scroll 50% 50% #DEEDF7;border: 1px solid #AED0EA;color: #222222;font-weight: bold;height:28px;">
  [@b.form name="indexForm" action="!index" target="main" theme="html"]
  <div style="margin-left:4px;margin-top:2px;float:left;line-height:16px;height:16px;">
    <label for="indexForm.session.id">审核批次:</label>
    [#if sessions?size == 0]
      <select id="indexForm.session.id" name="session.id" style="width:200px;">
        <option>缺少毕业审核批次</option>
      </select>
    [#else]
      [#assign selectedId = Parameters['session.id']!sessions[0].id?string /]
      <select id="indexForm.session.id" name="session.id" style="width:200px;" onchange="bg.form.submit(this.form)">
        [#list sessions as session]
        <option value="${session.id}"[#if session.id?string == selectedId] selected[/#if]>${session.name}</option>
        [/#list]
      </select>
    [/#if]
  </div>
  [@b.toolbar title=""]
    [#if sessions?size > 0]
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

  <table class="indexpanel">
    <tr>
      <td class="index_view" style="width:180px">
        [@b.form name="makeupCourseIndexForm" action="!search" title="ui.searchForm" target="makeupCourses" theme="search"]
          [#include "searchForm.ftl"/]
          <input type="hidden" name="makeupCourse.semester.id" value="${semester.id}">
        [/@]
      </td>
      <td class="index_content">[@b.div id="makeupCourses" href="!search?semester.id="+semester.id/]</td>
    </tr>
  </table>
[@b.foot/]
