[#ftl]
[@b.head/]
<style>
.reportTable {
    border-collapse: collapse;
    border:solid;
    border-width:1px;
    border-color:#006CB2;
    vertical-align: middle;
    font-style: normal;
    font-family:仿宋_GB2312;
    font-size: 10pt;
}
table.reportTable td{
    border:solid;
    border-width:1px;
    border-right-width:1;
    border-bottom-width:1;
    border-color:#006CB2;
}
</style>
[#macro emptyTd count]
     [#list 1..count as i]
     <td></td>
     [/#list]
[/#macro]
[#macro displayScore grade]
  [#if ((grade.getExamGrade(MAKEUP).examStatus.id)!0)!=1]
  ${grade.getExamGrade(MAKEUP).examStatus.name}
  [#else]
  ${grade.getScoreText(MAKEUP_GA)!}
  [/#if]
[/#macro]
[#assign pagePrintRow = 28 /]
   [#list makeupCourses  as task]
    [#assign grades=gradeMap.get(task)?sort_by(['std','user','code'])]
   [#assign pageNos=(grades?size/(pagePrintRow*2))?int /]
   [#if ((grades?size)>(pageNos*(pagePrintRow*2)))]
   [#assign pageNos=pageNos+1 /]
   [/#if]
   [#list 0..pageNos-1 as pageNo]
   [#assign passNo=pageNo*pagePrintRow*2 /]
    <table align="center" style="text-align:center" cellpadding="0" cellspacing="0">
        <tr>
            <td style="font-weight:bold;font-size:14pt" height="30px">${task.course.project.school.name}毕业补考成绩表<td>
        </tr>
        <tr>
            <td style="font-weight:bold;font-size:14px" >${task.semester.schoolYear}学年 ${task.semester.name}学期</td>
        </tr>
    </table>

     <table width='100%' align='center' border='0' style="font-family:仿宋_GB2312;font-size:14px">
        <tr height="10px">
            <td colspan="3"></td>
        </tr>
        <tr height="29px">
          <td width="40%">课程序号:${task.crn?if_exists}</td>
            <td width="25%">课程代码:${task.course.code}</td>
            <td>班级代码:[#list task.squads as adminClass]${adminClass.code}[#if adminClass_has_next],[/#if][/#list]</td>
        </tr>
        <tr height="29px">
            <td>班级名称:
            [#list task.squads as admin] ${admin.name!}[#if admin_has_next],[/#if][/#list]
             </td>
            <td>课程名称:${task.course.name}</td>
            <td align="left">人数:${task.stdCount}</td>
        </tr>
    </table>

   <table class="reportTable"  width="100%"  >
     <tr  height="29px" align="center">
       <td width="4%">序号</td>
       <td width="10%">学号</td>
       <td width="6%">姓名</td>
       <td width="8%">补考成绩</td>
       <td width="4%">序号</td>
       <td width="10%">学号</td>
       <td width="6%">姓名</td>
       <td width="8%" >补考成绩</td>
     </tr>
     [#list 0..pagePrintRow-1 as i]
     <tr height="29px" align="center" >
         [#if grades[i+passNo]?exists]
       <td>${i+1+passNo}</td>
       <td>${grades[i+passNo].std.user.code}</td>
       <td>${grades[i+passNo].std.user.name}</td>
       <td>
         [@displayScore grades[i+passNo]/]
       </td>
         [#else][@emptyTd count=4/][/#if]
         [#if grades[i+pagePrintRow+passNo]?exists]
       <td>${i+pagePrintRow+1+passNo}</td>
       <td>${grades[i+pagePrintRow+passNo].std.user.code}</td>
       <td>${grades[i+pagePrintRow+passNo].std.user.name}</td>
       <td>[@displayScore grades[i+pagePrintRow+passNo]/]</td>
         [#else]
          [@emptyTd count=4/]
         [/#if]
     </tr>
     [/#list]
     </table>
     <table align="center" class="reportFoot" width="100%">
      <tr>
      <td width="20%">统计人数:${grades?size}</td>
      <td width="20%"></td>
      <td width="30%">教师签名:</td>
      <td width="30%">成绩录入日期:____年__月__日</td>
    </tr>
  </table>
     [#if pageNo_has_next]<div style='PAGE-BREAK-AFTER: always'></div>[/#if]
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
