[#ftl]
[@b.head/]
<script language="JavaScript" type="text/JavaScript" src="${b.base}/static/scripts/grade/input.js?ver=20181123"></script>
[#macro gradeTd(grade, gradeType, courseTaker, index)]
    <td id="TD_${gradeType.id}_${courseTaker.std.id}">
      <input type="text" class="text"
        onfocus="this.style.backgroundColor='yellow'"
        onblur="this.style.backgroundColor='white';"
        tabIndex="${index+1}"
        onchange="checkScore(${index + 1}, this)"
        id="${gradeType.id}_${index + 1}" name="${gradeType.id}_${courseTaker.std.id}"
        value="[#if grade?string != "null"]${(grade.getScoreText(gradeType))?if_exists}[/#if]" style="width:80px" maxlength="10"/>
         [#if grade?string != "null" && (grade.getGrade(gradeType))??] [#local examGrade=grade.getGrade(gradeType)/][/#if]
    [@b.select items=examStatuses value=((examGrade.examStatus)!NormalExamStatus) name="examStatus_" + (gradeType.id)! + "_" + courseTaker.std.id id="examStatus_" + (gradeType.id)! + "_" + (index + 1) style="width:60px;"
      onchange="changeExamStatus('${(gradeType.id)!}_${index + 1}',this);checkScore(${index + 1}, this)" theme="html"/]
      <script language="javascript">gradeTable.add(${index}, "${courseTaker.std.id}", 1);</script>
    </td>
[/#macro]

[#macro displayGrades(index, courseTaker)]
    <td align="center">${index + 1}</td>
    <td>${courseTaker.std.code}</td>
    <td>${courseTaker.std.name}</td>
  [#if gradeMap.get(courseTaker.std)??]
  [#local grade = gradeMap.get(courseTaker.std)]
  [/#if]
    [#list gradeTypes as gradeType]
    [@gradeTd grade?default("null"), gradeType, courseTaker, index/]
    [/#list]
[/#macro]
    [@b.toolbar title=makeupCourse.course.name+"  毕业补考成绩录入"]
    bar.addBackOrClose();
    [/@]
    [#assign task=makeupCourse/]
 <script>
    gradeTable = new GradeTable();
    [#list gradeTypes as gradeType]
    gradeTable.gradeState[${gradeType_index}] = new Object();
    gradeTable.gradeState[${gradeType_index}].id = "${gradeType.id}";
    gradeTable.gradeState[${gradeType_index}].name = "${gradeType.id}";
    gradeTable.gradeState[${gradeType_index}].inputable=true;
    [/#list]
    gradeTable.precision=0;
    gradeTable.hasGa=false;
</script>
    <div align="center" style="font-size:15px;font-weight:bold">毕业补考成绩登记表<br>
        ${task.semester.schoolYear}学年[#if task.semester.name?contains("学期")]${task.semester.name}[#else]${makeupCourse.semester.name}学期[/#if]</font>
    </div>
    [#assign makeupTakes=makeupCourse.takers?sort_by(["std","code"])]
    [#if makeupTakes?size == 0]
        [#list 1..2 as i]<br>[/#list]
    <table width="90%" align="center" style="background-color:yellow">
        <tr style="color:red">
            <th>当前没有可以录入成绩的学生。<th>
        </tr>
    </table>
    [#list 1..2 as i]<br>[/#list]
    [/#if]

    <table width="90%" align="center" border="0" style="font-size:13px">
        <form name="gradeForm" method="post" action="" onsubmit="return false;">
            <input name="makeupCourseId" value="${task.id}" type="hidden"/>
        <tr>
            <td width="33%">课程序号:${task.crn?if_exists}</font></td>
            <td width="33%">课程代码:${task.course.code}</td>
            <td width="33%">课程名称:${task.course.name}</td>
        </tr>
        <tr>
           <td width="33%">班级：
              [#list task.squads as admin]
                ${admin.name!}[#if admin_has_next],[/#if]
             [/#list]
            </td>
            <td width="33%" > 阅卷教师：${(task.teacher.name)!}</td>
            <td>所录成绩:[#list gradeTypes as gradeType]${gradeType.name}[/#list]</td>
        </tr>
    </table>
    <div class="grid">
    <table  class="grid-table" align="center" style="width:90%;" onkeypress="gradeTable.onReturn.focus(event)">
        <tr align="center" class="grid-head">
        [#list 1..2 as i]
            <td align="center" width="80px">序号</td>
            <td align="center" width="120px">学号</td>
            <td width="100px">姓名</td>
            [#list gradeTypes as gradeType]
            <td>${gradeType.name}</td>
            [/#list]
        [/#list]
        </tr>
        [#assign makeupTakes = makeupTakes?sort_by(["std", "code"])?if_exists/]
        [#assign pageSize = ((makeupTakes?size + 1) / 2)?int/]
        [#list makeupTakes as courseTake]
        <tr>
            [@displayGrades courseTake_index, makeupTakes[courseTake_index]/]
            [#assign j = courseTake_index + pageSize/]
            [#if makeupTakes[j]?exists]
                [@displayGrades j, makeupTakes[j]/]
            [#else]
                [#list 1..4  as i]<td></td>[/#list]
            [/#if]
            [#if !makeupTakes[courseTake_index + 1]?exists || ((courseTake_index + 1) * 2 >= makeupTakes?size)]
        </tr>
                [#break]
            [/#if]
        </tr>
        [/#list]
        </form>
    </table>
    <table width="100%" style="font-size:15px" height="70px">
        <tr>
            <td align="center" id="submitTd"><button onclick="saveGrade(true)" id="bnJustSave">暂存</button> &nbsp;&nbsp;&nbsp; <button onclick="saveGrade(false)" id="bnSubmit">提交</button></td>
        </tr>
    </table>
    </div>
<script>
    gradeTable.changeTabIndex(document.gradeForm,true);
    var isOperation = false;
    function saveGrade(justSave) {
        if (isOperation) {
            alert("操作正在执行中，请稍候。");
            return;
        }
        isOperation = true;
        setTimeout("clearOperation()", 3000);
        var form = document.gradeForm;

        for (var i = 1; i <= ${makeupTakes?size}; i++) {
            [#list gradeTypes as gradeType]
            var score=$("#${gradeType.id}_" + i).val();
            if (null != $("${gradeType.id}_" + i) && "" != score && !/^\d+$/.test(score)) {
                alert("序号为" + i + "的成绩“" + score + "”不是有效的0或正整数，请检查。");
                $("#${gradeType.id}_" + i).val("");
                return;
            }
            [/#list]
        }
        form.action = "${b.url('!save')}";
        if (!justSave) {
            if (gradeTable.hasEmpty()) {
                clearOperation()
                alert("当前成绩中有没有录入的,请完成录入。");
                return;
            }
        }
        bg.form.addInput(form,"justSave", justSave, "hidden");
        document.getElementById("submitTd").innerHTML = "成绩" + (justSave ? "暂存" : "提交" ) + "中，请稍侯……";
        form.submit();
    }

    function clearOperation() {
        isOperation = false;
    }
</script>
</body>
[@b.foot/]
