const { createCanvas } = require('canvas');
const fs = require('fs');
const path = require('path');

function createDefaultImage(width, height, text, filename) {
  const canvas = createCanvas(width, height);
  const ctx = canvas.getContext('2d');

  // 填充背景
  ctx.fillStyle = '#f0f2f5';
  ctx.fillRect(0, 0, width, height);

  // 添加文字
  ctx.fillStyle = '#909399';
  ctx.font = '24px Arial';
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText(text, width / 2, height / 2);

  // 保存图片
  const buffer = canvas.toBuffer('image/png');
  fs.writeFileSync(path.join(__dirname, filename), buffer);
}

// 生成默认图片
createDefaultImage(1920, 600, '轮播图1 - 预约挂号', 'default-carousel1.png');
createDefaultImage(1920, 600, '轮播图2 - 专业医师', 'default-carousel2.png');
createDefaultImage(1920, 600, '轮播图3 - 智慧医疗', 'default-carousel3.png');
createDefaultImage(800, 600, '医院外观', 'default-hospital.png'); 