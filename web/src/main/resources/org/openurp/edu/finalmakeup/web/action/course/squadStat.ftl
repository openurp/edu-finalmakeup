[#ftl]
[@b.head/]
[@b.toolbar title="${semester.schoolYear}学年度${semester.name}学期毕业补考班级统计"]
  bar.addBackOrClose();
[/@]

[@b.form action="!squadStat" theme="list"]
  <input type="hidden" name="batch.id" value="${Parameters['batch.id']}"/>
  [@b.select items=departments name="department.id" value=department! label="院系"  style="width:200px" empty="..." onchange="bg.form.submit(this.form)"/]
[/@]
<div class="container">
  <table class="gridtable" style="border:0.5px solid #006CB2">
   <thead  class="gridhead">
    <tr>
      <td width="40px">序号</td>
      <td>班级</td>
      <td width="40px">总计</td>
      [#list matrix.courses as c]
      <td><span style="font-size:0.8em">${c.name}</span></td>
      [/#list]
    </tr>
   </thead>
   <tbody>
    [#list matrix.squads as squad]
    <tr>
      <td>${squad_index+1}</td>
      <td><span style="font-size:0.8em">${squad.name}</span></td>
      <td>${matrix.get(squad,null)}</td>
      [#list matrix.courses as c]
      <td>[#assign cnt=matrix.get(squad,c)/][#if cnt>0]${cnt}[/#if]</td>
      [/#list]
    </tr>
    [/#list]
    <tr>
      <td colspan="2">总计</td>
      <td>${matrix.get(null,null)}</td>
      [#list matrix.courses as c]
      <td>[#assign cnt=matrix.get(null,c)/][#if cnt>0]${cnt}[/#if]</td>
      [/#list]
    </tr>
   </tbody>
  </table>
</container>
[@b.foot/]
