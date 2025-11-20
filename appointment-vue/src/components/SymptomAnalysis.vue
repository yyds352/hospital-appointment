<template>
  <div class="symptom-analysis">
    <div class="analysis-header">
      <h3>症状分析助手</h3>
      <p>描述您的症状，AI智能推荐合适的科室</p>
    </div>

    <div class="analysis-form">
      <div class="form-group">
        <label for="symptoms">症状描述：</label>
        <textarea
          id="symptoms"
          v-model="symptoms"
          placeholder="请详细描述您的症状，例如：最近几天一直咳嗽，有痰，感觉有点发烧..."
          rows="4"
          class="symptoms-input"
        ></textarea>
        <div class="input-tips">
          <span>💡 小贴士：描述越详细，推荐越准确</span>
        </div>
      </div>

      <div class="form-actions">
        <button 
          @click="analyzeSymptoms" 
          :disabled="isAnalyzing || !symptoms.trim()"
          class="analyze-btn"
        >
          <span v-if="isAnalyzing">分析中...</span>
          <span v-else>🔍 智能分析</span>
        </button>
        <button 
          @click="clearForm" 
          class="clear-btn"
        >
          清空
        </button>
      </div>
    </div>

    <!-- 关键词提取结果 -->
    <div v-if="keywords.length > 0" class="keywords-section">
      <h4>📝 提取的关键词</h4>
      <div class="keywords-list">
        <span 
          v-for="keyword in keywords" 
          :key="keyword"
          class="keyword-tag"
        >
          {{ keyword }}
        </span>
      </div>
    </div>

    <!-- 推荐结果 -->
    <div v-if="recommendations.length > 0" class="recommendations-section">
      <h4>🏥 推荐科室</h4>
      <div class="recommendations-list">
        <div 
          v-for="(recommendation, index) in recommendations" 
          :key="recommendation.departmentId"
          class="recommendation-card"
          :class="{ 'top-recommendation': index === 0 }"
        >
          <div class="recommendation-header">
            <div class="department-info">
              <h5>{{ recommendation.departmentName }}</h5>
              <div class="match-score">
                <span class="score-label">匹配度：</span>
                <span class="score-value">{{ recommendation.matchScore }}%</span>
                <div class="score-bar">
                  <div 
                    class="score-fill" 
                    :style="{ width: recommendation.matchScore + '%' }"
                  ></div>
                </div>
              </div>
            </div>
            <div class="rank-badge" v-if="index === 0">
              🥇 最推荐
            </div>
            <div class="rank-badge" v-else-if="index === 1">
              🥈 次推荐
            </div>
            <div class="rank-badge" v-else-if="index === 2">
              🥉 备选
            </div>
          </div>

          <div class="recommendation-body">
            <p class="department-description">
              {{ recommendation.departmentDescription }}
            </p>
            
            <div class="matched-keywords">
              <span class="matched-label">匹配关键词：</span>
              <span 
                v-for="keyword in recommendation.matchedKeywords" 
                :key="keyword"
                class="matched-keyword"
              >
                {{ keyword }}
              </span>
            </div>

            <div class="recommendation-reason">
              <span class="reason-label">推荐理由：</span>
              <span class="reason-text">{{ recommendation.reason }}</span>
            </div>
          </div>

          <div class="recommendation-actions">
            <button 
              @click="selectDepartment(recommendation)"
              class="select-btn"
            >
              选择此科室
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 错误信息 -->
    <div v-if="error" class="error-message">
      <span>❌ {{ error }}</span>
      <button @click="clearError" class="close-error">×</button>
    </div>
  </div>
</template>

<script>
import { ref, reactive, toRefs } from 'vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'SymptomAnalysis',
  emits: ['department-selected'],
  setup(props, { emit }) {
    const state = reactive({
      symptoms: '',
      isAnalyzing: false,
      keywords: [],
      recommendations: [],
      error: null
    })

    const analyzeSymptoms = async () => {
      if (!state.symptoms.trim()) {
        ElMessage.warning('请输入症状描述')
        return
      }

      state.isAnalyzing = true
      state.error = null

      try {
        // 调用症状分析API
        const response = await fetch('/api/symptom-analysis/analyze', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            symptoms: state.symptoms
          })
        })

        if (!response.ok) {
          throw new Error('分析失败，请稍后重试')
        }

        const data = await response.json()
        
        if (data.success) {
          state.keywords = data.extractedKeywords || []
          state.recommendations = data.recommendations || []
          
          if (state.recommendations.length === 0) {
            ElMessage.info('未找到匹配的科室，建议咨询导诊台')
          }
        } else {
          throw new Error(data.error || '分析失败')
        }
      } catch (error) {
        console.error('症状分析错误:', error)
        state.error = error.message
        ElMessage.error('症状分析失败，请稍后重试')
      } finally {
        state.isAnalyzing = false
      }
    }

    const selectDepartment = (recommendation) => {
      emit('department-selected', {
        departmentId: recommendation.departmentId,
        departmentName: recommendation.departmentName,
        matchScore: recommendation.matchScore
      })
      ElMessage.success(`已选择 ${recommendation.departmentName}`)
    }

    const clearForm = () => {
      state.symptoms = ''
      state.keywords = []
      state.recommendations = []
      state.error = null
    }

    const clearError = () => {
      state.error = null
    }

    return {
      ...toRefs(state),
      analyzeSymptoms,
      selectDepartment,
      clearForm,
      clearError
    }
  }
}
</script>

<style scoped>
.symptom-analysis {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  max-width: 800px;
  margin: 0 auto;
}

.analysis-header {
  text-align: center;
  margin-bottom: 24px;
}

.analysis-header h3 {
  color: #2c3e50;
  font-size: 24px;
  margin-bottom: 8px;
}

.analysis-header p {
  color: #7f8c8d;
  font-size: 14px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #2c3e50;
  font-weight: 500;
}

.symptoms-input {
  width: 100%;
  padding: 12px;
  border: 2px solid #e1e8ed;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  transition: border-color 0.3s;
}

.symptoms-input:focus {
  outline: none;
  border-color: #3498db;
}

.input-tips {
  margin-top: 8px;
  font-size: 12px;
  color: #95a5a6;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 24px;
}

.analyze-btn, .clear-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.analyze-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.analyze-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.analyze-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.clear-btn {
  background: #ecf0f1;
  color: #7f8c8d;
}

.clear-btn:hover {
  background: #bdc3c7;
  color: #2c3e50;
}

.keywords-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.keywords-section h4 {
  color: #2c3e50;
  margin-bottom: 12px;
  font-size: 16px;
}

.keywords-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  background: #3498db;
  color: white;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.recommendations-section {
  margin-bottom: 24px;
}

.recommendations-section h4 {
  color: #2c3e50;
  margin-bottom: 16px;
  font-size: 18px;
}

.recommendation-card {
  background: white;
  border: 2px solid #e1e8ed;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  transition: all 0.3s;
}

.recommendation-card.top-recommendation {
  border-color: #f39c12;
  background: linear-gradient(135deg, #fff8e1 0%, #ffffff 100%);
}

.recommendation-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.department-info h5 {
  color: #2c3e50;
  font-size: 18px;
  margin-bottom: 8px;
}

.match-score {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.score-label {
  color: #7f8c8d;
}

.score-value {
  color: #27ae60;
  font-weight: bold;
  font-size: 16px;
}

.score-bar {
  width: 60px;
  height: 4px;
  background: #ecf0f1;
  border-radius: 2px;
  overflow: hidden;
}

.score-fill {
  height: 100%;
  background: linear-gradient(90deg, #e74c3c 0%, #f39c12 50%, #27ae60 100%);
  transition: width 0.3s;
}

.rank-badge {
  background: #f39c12;
  color: white;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: bold;
}

.recommendation-body {
  margin-bottom: 16px;
}

.department-description {
  color: #7f8c8d;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 12px;
}

.matched-keywords {
  margin-bottom: 12px;
}

.matched-label {
  color: #2c3e50;
  font-weight: 500;
  margin-right: 8px;
}

.matched-keyword {
  background: #e8f5e8;
  color: #27ae60;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  margin-right: 6px;
}

.recommendation-reason {
  background: #f8f9fa;
  padding: 8px 12px;
  border-radius: 8px;
  border-left: 4px solid #3498db;
}

.reason-label {
  color: #2c3e50;
  font-weight: 500;
  margin-right: 8px;
}

.reason-text {
  color: #7f8c8d;
  font-size: 14px;
}

.recommendation-actions {
  text-align: right;
}

.select-btn {
  background: #27ae60;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.select-btn:hover {
  background: #219a52;
  transform: translateY(-1px);
}

.error-message {
  background: #e74c3c;
  color: white;
  padding: 12px 16px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.close-error {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background 0.3s;
}

.close-error:hover {
  background: rgba(255, 255, 255, 0.2);
}

@media (max-width: 768px) {
  .symptom-analysis {
    padding: 16px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .recommendation-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .recommendation-actions {
    text-align: center;
  }
}
</style>