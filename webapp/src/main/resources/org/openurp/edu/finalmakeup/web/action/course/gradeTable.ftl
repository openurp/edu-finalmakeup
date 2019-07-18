[#ftl]
[@b.head/]
<style type="text/css">
.reportBody {
    border:solid;
    border-color:#006CB2;
    border-collapse: collapse;
    border-width:2px;
    vertical-align: middle;
    font-style: normal;
    font-size: 13px;
    font-family:宋体;
    table-layout: fixed;
    text-align:center;
}
table.reportBody td{
    border-style:solid;
    border-color:#006CB2;
    border-width:0 1px 1px 0;
}

table.reportBody td.columnIndex{
    border-width:0 1px 1px 2px;
}

table.reportBody tr{
  height:20px;
}
table.reportTitle tr{
  height:20px;
  border-width:1px;
  font-size:13px;
}
tr.columnTitle td{
  border-width:1px 1px 2px 1px;
}

tr.columnTitle td.columnIndexTitle{
  border-width:1px 1px 2px 2px;
  font-size:12px;
}

table.reportFoot{
  margin-bottom:20px;
}
table.reportFoot.tr {
}
</style>
[#macro emptyTd count]
     [#list 1..count as i]
     <td></td>
     [/#list]
[/#macro]
[#assign pagePrintRow = 28 /]
[#list makeupCourses  as task ]
 [#assign takes=task.takers?sort_by(['std','user','code'])]
    <br>
     <table width="100%" align="center" border="0"  >
     <tr>
      <td align="center" colspan="5" style="font-size:17pt" >
       <B>毕业补考成绩登记表</B>
      </td>
     </tr>
     <tr><td colspan="5">&nbsp;</td></tr>
   </table>
   [#assign pageNos=(takes?size/(pagePrintRow*2))?int /]
   [#if ((takes?size)>(pageNos*(pagePrintRow*2)))]
   [#assign pageNos=pageNos+1 /]
   [/#if]
   [#list 0..pageNos-1 as pageNo]
   [#assign passNo=pageNo*pagePrintRow*2 /]
   <table width="100%" align="center" border="0"  >
   <tr class="infoTitle">
         <td >学年学期:${task.semester.schoolYear}学年 ${task.semester.name}</td>
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
     <tr align="center">
       <td width="4%">序号</td>
       <td width="9%">学号</td>
       <td width="9%">姓名</td>
       <td width="8%">补考成绩</td>
       <td width="8%">备注</td>

       <td width="4%">序号</td>
       <td width="9%">学号</td>
       <td width="9%">姓名</td>
       <td width="8%">补考成绩</td>
       <td width="8%">备注</td>
     </tr>
     [#list 0..pagePrintRow-1 as i]
     <tr class="brightStyle" >
         [#if takes[i+passNo]?exists]
       <td>${i+1+passNo}</td>
       <td>${takes[i+passNo].std.user.code}</td>
       <td>${takes[i+passNo].std.user.name}</td>
       [@emptyTd count=2/]
         [/#if]

         [#if takes[i+pagePrintRow+passNo]?exists]
       <td>${i+pagePrintRow+1+passNo}</td>
       <td>${takes[i+pagePrintRow+passNo].std.user.code}</td>
       <td>${takes[i+pagePrintRow+passNo].std.user.name}</td>
         [@emptyTd count=2/]
         [#elseif takes[i+passNo]?exists]
          [@emptyTd count=5/]
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
     [#if pageNo_has_next]
    <div style='PAGE-BREAK-AFTER: always'></div>
    [/#if]
     [#if !pageNo_has_next]
        [#if task_has_next]<div style='PAGE-BREAK-AFTER: always'></div>[/#if]
     [/#if]
     [/#list]
[/#list]
   <table width="100%" align="center">
     <tr>
       <td align="center">
       <button onclick="print()"  class="notprint" >打印</button>
      </td>
    </tr>
  </table>
[@b.foot/]
