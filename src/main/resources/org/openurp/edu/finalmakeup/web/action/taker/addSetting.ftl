[#ftl]
[@b.head/]
[@b.toolbar title="添加毕业补考学生"]
  bar.addBack();
[/@]
[@b.form action="!addTakers" theme="list"]
   [@b.select items=tasks label="1)补考课程序号" option=r"${item.crn} ${item.course.name}" name="makeupCourse.id" empty="..." required="false" style="width:300px"/]
   [@b.textfield name="courseCode" label="2)补考课程代码"  maxlength="20" required="false" comment="补考课程序号或补考课程代码二选一"/]
   [@b.textarea name="stdCodes" label="学号" comment="(多个以单个空格 或者半角逗号结束,)" maxlength="800" rows="3" cols="80"/]
   [@b.formfoot]
     <input type="hidden" name="semester.id" value="${Parameters['makeupTaker.makeupCourse.semester.id']}" />
     [@b.submit value="提交"/]
   [/@]
[/@]
[@b.foot/]
