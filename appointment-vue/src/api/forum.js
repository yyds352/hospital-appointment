import request from '@/utils/request'

/**
 * 获取论坛帖子列表
 * @param {Object} params 查询参数，包含分页信息、排序条件等
 * @returns {Promise} 帖子列表
 */
export function getPosts(params) {
  return request({
    url: '/forum/posts',
    method: 'get',
    params
  })
}

/**
 * 获取帖子详情
 * @param {number} id 帖子ID
 * @returns {Promise} 帖子详情，包含评论列表
 */
export function getPostDetail(id) {
  return request({
    url: `/forum/posts/${id}`,
    method: 'get'
  })
}

/**
 * 发布新帖子
 * @param {Object} data 帖子数据，包含标题、内容等
 * @returns {Promise} 请求结果
 */
export function createForumPost(data) {
  return request({
    url: '/forum/posts',
    method: 'post',
    data
  })
}

/**
 * 更新帖子
 * @param {number} id 帖子ID
 * @param {Object} data 更新数据
 * @returns {Promise} 请求结果
 */
export function updateForumPost(id, data) {
  return request({
    url: `/forum/posts/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除帖子
 * @param {number} id 帖子ID
 * @returns {Promise} 请求结果
 */
export function deleteForumPost(id) {
  // 获取用户信息来判断权限
  const userInfo = localStorage.getItem('userInfo');
  let isAdmin = false;
  
  try {
    if (userInfo) {
      const user = JSON.parse(userInfo);
      isAdmin = user.role === 'ADMIN' || user.roles?.includes('ADMIN');
    }
  } catch (e) {
    console.log('解析用户信息失败，使用默认策略');
  }
  
  // 如果是管理员，直接尝试管理员接口
  if (isAdmin) {
    return request({
      url: `/admin/forum/posts/${id}`,
      method: 'delete'
    }).catch(error => {
      // 管理员接口失败，尝试普通接口
      console.log(`管理员删除失败，尝试用户接口: ${error.message || error}`);
      return request({
        url: `/forum/posts/${id}`,
        method: 'delete'
      });
    });
  }
  
  // 普通用户直接使用用户接口，避免403错误
  return request({
    url: `/forum/posts/${id}`,
    method: 'delete'
  });
}

/**
 * 发表评论
 * @param {number} postId 帖子ID
 * @param {Object} data 评论数据
 * @returns {Promise} 请求结果
 */
export function createComment(postId, data) {
  return request({
    url: `/forum/posts/${postId}/comments`,
    method: 'post',
    data
  })
}

/**
 * 删除评论
 * @param {number} postId 帖子ID
 * @param {number} commentId 评论ID
 * @returns {Promise}
 */
export function deleteComment(postId, commentId) {
  if (!postId || !commentId) {
    console.error('删除评论时参数不完整', { postId, commentId });
    return Promise.reject(new Error('参数不完整'));
  }
  
  console.log(`删除评论 - postId: ${postId}, commentId: ${commentId}`);
  
  return request({
    url: `/forum/posts/${postId}/comments/${commentId}`,
    method: 'delete'
  });
}

/**
 * 点赞帖子
 * @param {number} id 帖子ID
 * @returns {Promise} 请求结果
 */
export function likePost(id) {
  return request({
    url: `/forum/posts/${id}/like`,
    method: 'post'
  })
}

/**
 * 取消帖子点赞
 * @param {number} postId 帖子ID
 * @returns {Promise} 请求结果
 */
export function unlikePost(postId) {
  return request({
    url: `/forum/posts/${postId}/unlike`,
    method: 'post'
  })
}

/**
 * 点赞评论
 * @param {number} commentId 评论ID
 * @returns {Promise} 请求结果
 */
export function likeComment(commentId) {
  return request({
    url: `/comments/${commentId}/like`,
    method: 'post'
  })
}

/**
 * 取消评论点赞
 * @param {number} commentId 评论ID
 * @returns {Promise} 请求结果
 */
export function unlikeComment(commentId) {
  return request({
    url: `/comments/${commentId}/unlike`,
    method: 'post'
  })
}

// 获取我的帖子列表
export function getMyPosts(params) {
  return request({
    url: '/forum/my-posts',
    method: 'get',
    params
  })
}

/**
 * 获取评论列表
 * @param {number} postId 帖子ID
 * @param {Object} params 查询参数
 * @returns {Promise} 评论列表
 */
export function getComments(postId, params) {
  return request({
    url: `/forum/posts/${postId}/comments`,
    method: 'get',
    params
  }).then(response => {
    return response;
  }).catch(error => {
    console.error('获取评论失败:', error);
    throw error;
  });
}

// 搜索帖子
export function searchPosts(keyword, params) {
  return request({
    url: '/posts/search',
    method: 'get',
    params: { ...params, keyword }
  })
}

// 获取用户的帖子
export function getUserPosts(userId, params) {
  return request({
    url: `/posts/user/${userId}`,
    method: 'get',
    params
  })
}



// 获取所有分类
export function getCategories() {
  return request({
    url: '/post-categories',
    method: 'get'
  })
}

// 获取分类详情
export function getCategoryById(id) {
  return request({
    url: `/post-categories/${id}`,
    method: 'get'
  })
}

// 创建分类
export function createCategory(data) {
  return request({
    url: '/post-categories',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(id, data) {
  return request({
    url: `/post-categories/${id}`,
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(id) {
  return request({
    url: `/post-categories/${id}`,
    method: 'delete'
  })
}

// 获取论坛统计数据
export function getForumStats() {
  return request({
    url: '/forum/stats',
    method: 'get'
  })
}

/**
 * 收藏帖子
 * @param {number} id 帖子ID
 * @returns {Promise} 收藏结果
 */
export function favoritePost(id) {
  return request({
    url: `/forum/posts/${id}/favorite`,
    method: 'post'
  })
}

/**
 * 取消收藏帖子
 * @param {number} id 帖子ID
 * @returns {Promise} 取消收藏结果
 */
export function unfavoritePost(id) {
  return request({
    url: `/forum/posts/${id}/favorite`,
    method: 'delete'
  })
}

/**
 * 获取收藏状态
 * @param {number} id 帖子ID
 * @returns {Promise} 是否已收藏
 */
export function checkFavoriteStatus(id) {
  return request({
    url: `/forum/posts/${id}/favorite`,
    method: 'get'
  })
}

/**
 * 获取用户的收藏列表
 * @param {Object} params 查询参数
 * @returns {Promise} 收藏列表
 */
export function getUserFavorites(params) {
  return request({
    url: '/forum/favorites',
    method: 'get',
    params
  })
}

/**
 * 管理员批准帖子
 * @param {number} id 帖子ID
 */
export function approvePost(id) {
  return request({
    url: `/admin/forum/posts/${id}/approve`,
    method: 'post'
  })
}

/**
 * 管理员隐藏帖子
 * @param {number} id 帖子ID
 */
export function hidePost(id) {
  return request({
    url: `/admin/forum/posts/${id}/hide`,
    method: 'post'
  })
}

/**
 * 获取帖子举报信息
 * @param {number} id 帖子ID
 */
export function getPostReports(id) {
  return request({
    url: `/admin/forum/posts/${id}/reports`,
    method: 'get'
  })
}

/**
 * 忽略特定举报
 * @param {number} reportId 举报ID
 */
export function dismissReport(reportId) {
  return request({
    url: `/admin/forum/reports/${reportId}/dismiss`,
    method: 'post'
  })
}

/**
 * 忽略所有对某帖子的举报
 * @param {number} postId 帖子ID
 */
export function dismissAllReports(postId) {
  return request({
    url: `/admin/forum/posts/${postId}/dismissReports`,
    method: 'post'
  })
}

/**
 * 获取所有帖子分类
 */
export function getAllCategories() {
  return getCategories()
}

/**
 * 获取论坛管理帖子列表 (管理员专用)
 */
export function getForumPosts(params) {
  return request({
    url: '/admin/forum/posts',
    method: 'get',
    params
  })
}

/**
 * 直接获取评论列表（不使用分页）
 * @param {number} postId 帖子ID
 * @returns {Promise} 包含评论列表和总数信息的对象
 */
export function getCommentsDirect(postId) {
  return request({
    url: `/forum/posts/${postId}/comments`,
    method: 'get',
    params: {
      size: 100 // 获取较多评论，模拟直接获取效果
    }
  }).then(response => {
    console.log('📥 getCommentsDirect原始响应:', response);
    
    // 如果response本身就是数组，直接返回
    if (Array.isArray(response)) {
      console.log('✅ 检测到数组格式，评论数:', response.length);
      return {
        content: response,
        totalElements: response.length,
        totalPages: 1,
        numberOfElements: response.length
      };
    }
    
    // 详细检查响应内容
    if (!response) {
      console.error('❌ 响应对象为空');
      return {
        content: [],
        totalElements: 0,
        totalPages: 0,
        numberOfElements: 0
      };
    }
    
    // 🎯 首先检查是否是标准API响应格式（code: 200, data: {...}）
    if (response.code === 200 && response.data) {
      console.log('✅ 检测到标准API响应格式');
      const apiData = response.data;
      
      // 检查data字段是否是Spring Data Page格式
      if (apiData.content && Array.isArray(apiData.content) && apiData.totalElements !== undefined) {
        console.log('✅ data字段是Spring Data Page格式，评论数:', apiData.content.length);
        return apiData;
      }
      
      // 如果data字段是数组
      if (Array.isArray(apiData)) {
        console.log('✅ data字段是数组，评论数:', apiData.length);
        return {
          content: apiData,
          totalElements: apiData.length,
          totalPages: 1,
          numberOfElements: apiData.length
        };
      }
      
      // 其他情况，返回空结果
      console.log('⚠️ data字段格式未知，返回空结果');
      return {
        content: [],
        totalElements: 0,
        totalPages: 0,
        numberOfElements: 0
      };
    }
    
    // 然后检查是否是Spring Data Page格式（响应拦截器已处理过）
    if (typeof response === 'object' && 
        response.content && 
        Array.isArray(response.content) &&
        response.totalElements !== undefined) {
      console.log('✅ 检测到Spring Data Page格式，评论数:', response.content.length);
      return response;
    }
    
    // 处理可能还包含data属性的情况（兼容性处理）
    if (response.data) {
      console.log('🔍 检测到response.data，进行兼容性处理...');
      
      if (Array.isArray(response.data)) {
        console.log('✅ response.data是数组，评论数:', response.data.length);
        return {
          content: response.data,
          totalElements: response.data.length,
          totalPages: 1,
          numberOfElements: response.data.length
        };
      } else if (response.data.content && Array.isArray(response.data.content)) {
        console.log('✅ response.data是Spring Data Page格式');
        return response.data;
      } else if (typeof response.data === 'object' && 
                response.data.pageable && 
                response.data.totalElements !== undefined && 
                response.data.content) {
        console.log('✅ response.data是Spring Data Page格式');
        return response.data;
      } else {
        // 最后尝试查找任何数组
        console.log('🔍 在response.data中查找数组...');
        for (const key in response.data) {
          if (Array.isArray(response.data[key])) {
            const content = response.data[key];
            console.log(`✅ 找到数组属性 ${key}，评论数:`, content.length);
            return {
              content: content,
              totalElements: content.length,
              totalPages: 1,
              numberOfElements: content.length
            };
          }
        }
      }
    }
    
    console.log('⚠️ 未识别的响应格式，返回空结果');
    return {
      content: [],
      totalElements: 0,
      totalPages: 0,
      numberOfElements: 0
    };
  }).catch(error => {
    // 404错误是正常的，表示该接口不存在或没有评论数据
    if (error.response && error.response.status === 404) {
      console.log(`帖子 ${postId} 没有评论数据`);
      return {
        content: [],
        totalElements: 0,
        totalPages: 0,
        numberOfElements: 0
      };
    }
    
    console.error('❌ 直接获取评论失败:', error);
    throw error;
  });
}