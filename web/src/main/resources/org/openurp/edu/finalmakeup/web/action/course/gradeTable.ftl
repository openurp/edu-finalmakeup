[#ftl]
[@b.head/]
[@b.toolbar title="毕业补考成绩登分册"]
   bar.addPrint();
   bar.addClose();
[/@]
<style type="text/css">
.reportBody {
    border-collapse: collapse;
    vertical-align: middle;
    font-style: normal;
    font-size: 9pt;
    font-family:宋体;
    table-layout: fixed;
    text-align:center;
    white-space: nowrap;
}
table.reportBody td{
    border-style:solid;
    border-color:#006CB2;
    border-width:1px 1px;
}

table.reportBody td.columnIndex{
    border-width:0 1px 1px 1px;
}

table.reportBody td.columnSeparator{
  border-width:0 0px 0px 0;
  width:0.5%;
}

table.reportBody tr{
  height:22pt;
}
table.reportTitle {
  margin-top:10px;
}
table.reportTitle tr{
  border-width:1px;
  font-size:11pt;
  font-weight:bold;
  font-family:黑体
}
tr.columnTitle td{
  border-width:1px 1px 1px 1px;
  word-break: break-all;
  white-space:pre-wrap;
  font-size:9pt;
  font-family:黑体
}

table.reportFoot{
  font-size:12pt;
  font-family:黑体
}

table.reportFoot tr {
  height:40px;
}
.examStatus{

}
.longScoreText{
  font-size:10px;
}
</style>
[#macro emptyTd count]
     [#list 1..count as i]
     <td></td>
     [/#list]
[/#macro]
[#assign pagePrintRow = 25 /]
[#list makeupCourses  as task ]
 [#assign takes=task.takers?sort_by(['std','code'])]

   [#assign pageNos=(takes?size/(pagePrintRow*2))?int /]
   [#if ((takes?size)>(pageNos*(pagePrintRow*2)))]
   [#assign pageNos=pageNos+1 /]
   [/#if]
   [#list 0..pageNos-1 as pageNo]
   [#assign passNo=pageNo*pagePrintRow*2 /]
    <table align="center" style="text-align:center;margin-top: 15px;" cellpadding="0" cellspacing="0">
        <tr>
            <td style="font-weight:bold;font-size:22pt;font-family:宋体" height="30px">
            ${task.course.project.school.name}成绩登分表（补）
             <td>
        </tr>
        <tr>
          <td style="font-weight:bold;font-size:14pt;font-family:宋体" >（${(task.semester.schoolYear)?if_exists}学年${(task.semester.name)?if_exists?replace("0","第")}学期）</td>
        </tr>
    </table>
   <table width="100%" align="center" border="0"  >
   <tr class="infoTitle">
       <td >课程序号:${task.crn}</td>
       <td >课程代码:${task.course.code}</td>
       <td >课程名称:${task.course.name}</td>
   </tr>
   <tr class="infoTitle">
      <td>阅卷教师: ${(task.teacher.name)!}</td>
      <td>班级：
              [#list task.squads as admin]
                ${admin.name!}[#if admin_has_next],[/#if]
             [/#list]
        </td>
   </tr>
   </table>
   <table class="reportBody"  width="100%"  >
    <tr align="center" class="columnTitle">
      [#list 1..2 as i]
             <td class="columnIndexTitle" width="5%">序号</td>
             <td width="15%">学号</td>
             <td width="10%">姓名</td>
             <td width="10%">考试成绩</td>
             <td width="10%">备注</td>
      [/#list]
    </tr>
     [#list 0..pagePrintRow-1 as i]
     <tr class="brightStyle" >
       [#if takes[i+passNo]?exists]
       <td>${i+1+passNo}</td>
       <td>${takes[i+passNo].std.code}</td>
       <td>${takes[i+passNo].std.name}</td>
       [@emptyTd count=2/]

       [#if takes[i+pagePrintRow+passNo]?exists]
       <td>${i+pagePrintRow+1+passNo}</td>
       <td>${takes[i+pagePrintRow+passNo].std.code}</td>
       <td>${takes[i+pagePrintRow+passNo].std.name}</td>
       [@emptyTd count=2/]
       [#elseif takes[i+passNo]?exists]
          [@emptyTd count=5/]
       [/#if]
      [#else]
      [@emptyTd count=10/]
      [/#if]
     </tr>
     [/#list]
     </table>
     <table align="center" class="reportFoot" width="100%">
      <tr>
      <td width="20%">统计人数:${takes?size}</td>
      <td width="20%"></td>
      <td width="30%">教师签名:</td>
      <td width="30%">成绩录入日期:____年__月__日</td>
      </tr>
     </table>
     [#if pageNo_has_next]<div style='PAGE-BREAK-AFTER: always'></div>[/#if]
     [/#list]
   [#if task_has_next]<div style='PAGE-BREAK-AFTER: always'></div>[/#if]
[/#list]
[@b.foot/]
