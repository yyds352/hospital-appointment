package com.example.appointment.service.impl;

import com.example.appointment.entity.Department;
import com.example.appointment.repository.DepartmentRepository;
import com.example.appointment.service.SymptomAnalysisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SymptomAnalysisServiceImpl implements SymptomAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(SymptomAnalysisServiceImpl.class);
    
    private final DepartmentRepository departmentRepository;

    // 症状关键词与科室的映射关系
    private static final Map<String, List<Long>> SYMPTOM_DEPARTMENT_MAPPING = Map.ofEntries(
        Map.entry("发烧", List.of(1L)), // 内科
        Map.entry("发热", List.of(1L)),
        Map.entry("感冒", List.of(1L)),
        Map.entry("咳嗽", List.of(1L)),
        Map.entry("胸闷", List.of(1L)),
        Map.entry("气短", List.of(1L)),
        Map.entry("呼吸困难", List.of(1L)),
        Map.entry("心悸", List.of(1L)),
        Map.entry("头晕", List.of(1L)),
        Map.entry("头痛", List.of(1L)),
        Map.entry("恶心", List.of(1L)),
        Map.entry("呕吐", List.of(1L)),
        Map.entry("腹痛", List.of(1L)),
        Map.entry("腹泻", List.of(1L)),
        Map.entry("便秘", List.of(1L)),
        Map.entry("胃痛", List.of(1L)),
        Map.entry("胃胀", List.of(1L)),
        Map.entry("消化不良", List.of(1L)),
        
        Map.entry("外伤", List.of(2L)), // 外科
        Map.entry("骨折", List.of(2L)),
        Map.entry("扭伤", List.of(2L)),
        Map.entry("割伤", List.of(2L)),
        Map.entry("烧伤", List.of(2L)),
        Map.entry("烫伤", List.of(2L)),
        Map.entry("手术", List.of(2L)),
        Map.entry("阑尾", List.of(2L)),
        Map.entry("胆囊", List.of(2L)),
        Map.entry("结石", List.of(2L)),
        Map.entry("肿瘤", List.of(2L)),
        Map.entry("肿块", List.of(2L)),
        Map.entry("包块", List.of(2L)),
        
        Map.entry("儿童", List.of(3L)), // 儿科
        Map.entry("小孩", List.of(3L)),
        Map.entry("幼儿", List.of(3L)),
        Map.entry("婴儿", List.of(3L)),
        Map.entry("发育", List.of(3L)),
        Map.entry("疫苗", List.of(3L)),
        Map.entry("接种", List.of(3L)),
        Map.entry("生长", List.of(3L)),
        Map.entry("营养不良", List.of(3L)),
        Map.entry("佝偻病", List.of(3L)),
        Map.entry("手足口", List.of(3L)),
        Map.entry("水痘", List.of(3L)),
        Map.entry("腮腺炎", List.of(3L)),
        
        Map.entry("妇科", List.of(4L)), // 妇科
        Map.entry("月经", List.of(4L)),
        Map.entry("经期", List.of(4L)),
        Map.entry("痛经", List.of(4L)),
        Map.entry("月经不调", List.of(4L)),
        Map.entry("白带", List.of(4L)),
        Map.entry("阴道炎", List.of(4L)),
        Map.entry("盆腔炎", List.of(4L)),
        Map.entry("宫颈炎", List.of(4L)),
        Map.entry("子宫肌瘤", List.of(4L)),
        Map.entry("卵巢囊肿", List.of(4L)),
        Map.entry("乳腺增生", List.of(4L)),
        Map.entry("怀孕", List.of(4L)),
        Map.entry("妊娠", List.of(4L)),
        Map.entry("孕期", List.of(4L)),
        Map.entry("产检", List.of(4L)),
        Map.entry("分娩", List.of(4L)),
        
        Map.entry("眼睛", List.of(5L)), // 眼科
        Map.entry("视力", List.of(5L)),
        Map.entry("近视", List.of(5L)),
        Map.entry("远视", List.of(5L)),
        Map.entry("散光", List.of(5L)),
        Map.entry("白内障", List.of(5L)),
        Map.entry("青光眼", List.of(5L)),
        Map.entry("结膜炎", List.of(5L)),
        Map.entry("角膜炎", List.of(5L)),
        Map.entry("眼干", List.of(5L)),
        Map.entry("眼涩", List.of(5L)),
        Map.entry("流泪", List.of(5L)),
        Map.entry("畏光", List.of(5L)),
        Map.entry("复视", List.of(5L)),
        Map.entry("视物模糊", List.of(5L)),
        
        Map.entry("耳朵", List.of(6L)), // 耳鼻喉科
        Map.entry("听力", List.of(6L)),
        Map.entry("耳鸣", List.of(6L)),
        Map.entry("耳聋", List.of(6L)),
        Map.entry("中耳炎", List.of(6L)),
        Map.entry("外耳炎", List.of(6L)),
        Map.entry("鼻炎", List.of(6L)),
        Map.entry("鼻窦炎", List.of(6L)),
        Map.entry("过敏性鼻炎", List.of(6L)),
        Map.entry("鼻出血", List.of(6L)),
        Map.entry("鼻塞", List.of(6L)),
        Map.entry("流鼻涕", List.of(6L)),
        Map.entry("咽炎", List.of(6L)),
        Map.entry("扁桃体炎", List.of(6L)),
        Map.entry("喉炎", List.of(6L)),
        Map.entry("声音嘶哑", List.of(6L)),
        Map.entry("吞咽困难", List.of(6L)),
        
        Map.entry("皮肤", List.of(7L)), // 皮肤科
        Map.entry("皮疹", List.of(7L)),
        Map.entry("湿疹", List.of(7L)),
        Map.entry("痤疮", List.of(7L)),
        Map.entry("痘痘", List.of(7L)),
        Map.entry("皮炎", List.of(7L)),
        Map.entry("荨麻疹", List.of(7L)),
        Map.entry("过敏", List.of(7L)),
        Map.entry("瘙痒", List.of(7L)),
        Map.entry("红肿", List.of(7L)),
        Map.entry("脱皮", List.of(7L)),
        Map.entry("银屑病", List.of(7L)),
        Map.entry("白癜风", List.of(7L)),
        Map.entry("真菌感染", List.of(7L)),
        Map.entry("细菌感染", List.of(7L)),
        Map.entry("病毒感染", List.of(7L)),
        
        Map.entry("牙齿", List.of(8L)), // 口腔科
        Map.entry("牙痛", List.of(8L)),
        Map.entry("牙龈", List.of(8L)),
        Map.entry("牙龈出血", List.of(8L)),
        Map.entry("牙周炎", List.of(8L)),
        Map.entry("龋齿", List.of(8L)),
        Map.entry("蛀牙", List.of(8L)),
        Map.entry("智齿", List.of(8L)),
        Map.entry("拔牙", List.of(8L)),
        Map.entry("补牙", List.of(8L)),
        Map.entry("洗牙", List.of(8L)),
        Map.entry("矫正", List.of(8L)),
        Map.entry("种植牙", List.of(8L)),
        Map.entry("口腔溃疡", List.of(8L)),
        Map.entry("口臭", List.of(8L)),
        
        Map.entry("心理", List.of(9L)), // 心理咨询
        Map.entry("焦虑", List.of(9L)),
        Map.entry("抑郁", List.of(9L)),
        Map.entry("失眠", List.of(9L)),
        Map.entry("压力", List.of(9L)),
        Map.entry("情绪", List.of(9L)),
        Map.entry("烦躁", List.of(9L)),
        Map.entry("恐慌", List.of(9L)),
        Map.entry("恐惧", List.of(9L)),
        Map.entry("强迫", List.of(9L)),
        Map.entry("社交恐惧", List.of(9L)),
        Map.entry("精神紧张", List.of(9L)),
        Map.entry("心理创伤", List.of(9L)),
        Map.entry("心理咨询", List.of(9L))
    );

    @Override
    public List<Map<String, Object>> analyzeSymptomsAndRecommendDepartments(String symptoms) {
        log.info("开始分析症状: {}", symptoms);
        
        if (symptoms == null || symptoms.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // 提取关键词
        List<String> keywords = extractSymptomKeywords(symptoms);
        log.info("提取的关键词: {}", keywords);
        
        if (keywords.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取所有科室
        List<Department> departments = departmentRepository.findAll();
        
        // 计算每个科室的匹配度
        Map<Long, Integer> departmentScores = new HashMap<>();
        Map<Long, List<String>> matchedKeywords = new HashMap<>();
        
        for (String keyword : keywords) {
            List<Long> departmentIds = SYMPTOM_DEPARTMENT_MAPPING.get(keyword);
            if (departmentIds != null) {
                for (Long departmentId : departmentIds) {
                    departmentScores.merge(departmentId, 10, Integer::sum);
                    matchedKeywords.computeIfAbsent(departmentId, k -> new ArrayList<>()).add(keyword);
                }
            }
        }
        
        // 如果没有找到匹配的科室，返回所有科室（匹配度为0）
        if (departmentScores.isEmpty()) {
            return departments.stream()
                .map(dept -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("department", dept);
                    result.put("matchScore", 0);
                    result.put("matchedKeywords", Collections.emptyList());
                    result.put("reason", "未找到匹配的症状关键词");
                    return result;
                })
                .collect(Collectors.toList());
        }
        
        // 按匹配度排序并构建结果
        return departmentScores.entrySet().stream()
            .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
            .map(entry -> {
                Long departmentId = entry.getKey();
                Integer score = entry.getValue();
                
                Department department = departments.stream()
                    .filter(dept -> dept.getId().equals(departmentId))
                    .findFirst()
                    .orElse(null);
                
                if (department == null) {
                    return null;
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("department", department);
                result.put("matchScore", score);
                result.put("matchedKeywords", matchedKeywords.get(departmentId));
                result.put("reason", buildRecommendationReason(matchedKeywords.get(departmentId)));
                
                return result;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<String> extractSymptomKeywords(String symptoms) {
        if (symptoms == null || symptoms.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String cleanSymptoms = symptoms.toLowerCase()
            .replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", " ")
            .replaceAll("\\s+", " ");
        
        Set<String> keywords = new HashSet<>();
        
        // 精确匹配
        for (String key : SYMPTOM_DEPARTMENT_MAPPING.keySet()) {
            if (cleanSymptoms.contains(key)) {
                keywords.add(key);
            }
        }
        
        // 模糊匹配（对于较长的关键词）
        for (String key : SYMPTOM_DEPARTMENT_MAPPING.keySet()) {
            if (key.length() >= 3 && !keywords.contains(key)) {
                for (String word : cleanSymptoms.split(" ")) {
                    if (word.length() >= 2 && key.contains(word)) {
                        keywords.add(key);
                        break;
                    }
                }
            }
        }
        
        return new ArrayList<>(keywords);
    }
    
    @Override
    public int calculateMatchScore(List<String> keywords, Long departmentId) {
        if (keywords == null || keywords.isEmpty() || departmentId == null) {
            return 0;
        }
        
        int score = 0;
        for (String keyword : keywords) {
            List<Long> departmentIds = SYMPTOM_DEPARTMENT_MAPPING.get(keyword);
            if (departmentIds != null && departmentIds.contains(departmentId)) {
                score += 10;
            }
        }
        
        return Math.min(score, 100); // 最高分100
    }
    
    private String buildRecommendationReason(List<String> matchedKeywords) {
        if (matchedKeywords == null || matchedKeywords.isEmpty()) {
            return "基于症状分析推荐";
        }
        
        if (matchedKeywords.size() == 1) {
            return "检测到症状：" + matchedKeywords.get(0);
        } else if (matchedKeywords.size() == 2) {
            return "检测到症状：" + String.join("、", matchedKeywords);
        } else {
            return "检测到" + matchedKeywords.size() + "个相关症状";
        }
    }
}