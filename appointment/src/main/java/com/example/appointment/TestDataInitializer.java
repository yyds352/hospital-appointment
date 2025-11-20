package com.example.appointment;

import com.example.appointment.entity.Department;
import com.example.appointment.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TestDataInitializer implements CommandLineRunner {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有科室数据
        if (departmentRepository.count() == 0) {
            // 插入测试科室数据
            Department dept1 = new Department();
            dept1.setName("内科");
            dept1.setDescription("内科科室");
            dept1.setLocation("门诊楼1层");
            dept1.setStatus(1);
            dept1.setCode("NEIKE");
            dept1.setCreatedAt(LocalDateTime.now());
            dept1.setUpdatedAt(LocalDateTime.now());

            Department dept2 = new Department();
            dept2.setName("外科");
            dept2.setDescription("外科科室");
            dept2.setLocation("门诊楼2层");
            dept2.setStatus(1);
            dept2.setCode("WAIKE");
            dept2.setCreatedAt(LocalDateTime.now());
            dept2.setUpdatedAt(LocalDateTime.now());

            Department dept3 = new Department();
            dept3.setName("儿科");
            dept3.setDescription("儿科科室");
            dept3.setLocation("门诊楼3层");
            dept3.setStatus(1);
            dept3.setCode("ERKE");
            dept3.setCreatedAt(LocalDateTime.now());
            dept3.setUpdatedAt(LocalDateTime.now());

            Department dept4 = new Department();
            dept4.setName("妇产科");
            dept4.setDescription("妇产科科室");
            dept4.setLocation("门诊楼4层");
            dept4.setStatus(1);
            dept4.setCode("FUCHANKE");
            dept4.setCreatedAt(LocalDateTime.now());
            dept4.setUpdatedAt(LocalDateTime.now());

            Department dept5 = new Department();
            dept5.setName("眼科");
            dept5.setDescription("眼科科室");
            dept5.setLocation("门诊楼5层");
            dept5.setStatus(1);
            dept5.setCode("YANKE");
            dept5.setCreatedAt(LocalDateTime.now());
            dept5.setUpdatedAt(LocalDateTime.now());

            // 保存到数据库
            departmentRepository.save(dept1);
            departmentRepository.save(dept2);
            departmentRepository.save(dept3);
            departmentRepository.save(dept4);
            departmentRepository.save(dept5);

            System.out.println("测试科室数据已插入");
        }
    }
}