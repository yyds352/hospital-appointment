package com.example.appointment.controller;

import com.example.appointment.annotation.RequireRole;
import com.example.appointment.common.Result;
import com.example.appointment.dto.DepartmentDTO;
import com.example.appointment.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);
    private final DepartmentService departmentService;

    @PostMapping
    @RequireRole({"ADMIN"})
    public Result<DepartmentDTO> create(@RequestBody @Valid DepartmentDTO departmentDTO) {
        log.info("创建科室：{}", departmentDTO);
        return Result.success(departmentService.create(departmentDTO));
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<DepartmentDTO> update(@PathVariable Long id, @RequestBody @Valid DepartmentDTO departmentDTO) {
        log.info("更新科室，ID：{}，数据：{}", id, departmentDTO);
        return Result.success(departmentService.update(id, departmentDTO));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除科室，ID：{}", id);
        departmentService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DepartmentDTO> getById(@PathVariable Long id) {
        return Result.success(departmentService.getById(id));
    }

    @GetMapping
    public Result<Page<DepartmentDTO>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            Pageable pageable) {
        return Result.success(departmentService.search(name, status, pageable));
    }
}