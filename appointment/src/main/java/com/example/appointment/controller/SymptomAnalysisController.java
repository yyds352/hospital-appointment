package com.example.appointment.controller;

import com.example.appointment.entity.Department;
import com.example.appointment.service.SymptomAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/symptom-analysis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SymptomAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(SymptomAnalysisController.class);
    private final SymptomAnalysisService symptomAnalysisService;

    /**
     * 分析症状并推荐科室
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeSymptoms(@RequestBody Map<String, String> request) {
        try {
            String symptoms = request.get("symptoms");
            
            if (symptoms == null || symptoms.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "症状描述不能为空"
                ));
            }
            
            List<Map<String, Object>> recommendations = symptomAnalysisService.analyzeSymptomsAndRecommendDepartments(symptoms);
            
            // 转换响应格式
            List<Map<String, Object>> formattedRecommendations = recommendations.stream()
                .map(rec -> {
                    Department dept = (Department) rec.get("department");
                    Map<String, Object> formatted = new HashMap<>();
                    formatted.put("departmentId", dept.getId());
                    formatted.put("departmentName", dept.getName());
                    formatted.put("departmentDescription", dept.getDescription());
                    formatted.put("matchScore", rec.get("matchScore"));
                    formatted.put("matchedKeywords", rec.get("matchedKeywords"));
                    formatted.put("reason", rec.get("reason"));
                    return formatted;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("symptoms", symptoms);
            response.put("recommendations", formattedRecommendations);
            response.put("extractedKeywords", symptomAnalysisService.extractSymptomKeywords(symptoms));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("症状分析失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "症状分析失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取症状关键词
     */
    @PostMapping("/extract-keywords")
    public ResponseEntity<?> extractKeywords(@RequestBody Map<String, String> request) {
        try {
            String symptoms = request.get("symptoms");
            
            if (symptoms == null || symptoms.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "症状描述不能为空"
                ));
            }
            
            List<String> keywords = symptomAnalysisService.extractSymptomKeywords(symptoms);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("symptoms", symptoms);
            response.put("keywords", keywords);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("关键词提取失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "关键词提取失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 计算症状与科室的匹配度
     */
    @PostMapping("/calculate-match-score")
    public ResponseEntity<?> calculateMatchScore(@RequestBody Map<String, Object> request) {
        try {
            String symptoms = (String) request.get("symptoms");
            Integer departmentId = (Integer) request.get("departmentId");
            
            if (symptoms == null || symptoms.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "症状描述不能为空"
                ));
            }
            
            if (departmentId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "科室ID不能为空"
                ));
            }
            
            List<String> keywords = symptomAnalysisService.extractSymptomKeywords(symptoms);
            int score = symptomAnalysisService.calculateMatchScore(keywords, departmentId.longValue());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("symptoms", symptoms);
            response.put("departmentId", departmentId);
            response.put("keywords", keywords);
            response.put("matchScore", score);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("匹配度计算失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "匹配度计算失败: " + e.getMessage()
            ));
        }
    }
}