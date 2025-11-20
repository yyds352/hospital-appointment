package com.example.appointment.service;

import java.util.List;
import java.util.Map;

public interface SymptomAnalysisService {
    
    /**
     * 分析症状并推荐科室
     * @param symptoms 症状描述
     * @return 推荐的科室列表及匹配度
     */
    List<Map<String, Object>> analyzeSymptomsAndRecommendDepartments(String symptoms);
    
    /**
     * 获取症状关键词
     * @param symptoms 症状描述
     * @return 提取的关键词列表
     */
    List<String> extractSymptomKeywords(String symptoms);
    
    /**
     * 计算症状与科室的匹配度
     * @param keywords 症状关键词
     * @param departmentId 科室ID
     * @return 匹配度分数 (0-100)
     */
    int calculateMatchScore(List<String> keywords, Long departmentId);
}