/**
 * 二维码生成工具模块
 * 路径: seat_client/utils/qrcode/index.js
 * * 使用方法:
 * import QRCode from '../../utils/qrcode/index';
 * QRCode.draw('内容', 'canvasId', this);
 */

var QRCode = {};

(function () {
    // 这里是模拟的绘制逻辑
    // 在实际开发中，您可以将 GitHub 上下载的 weapp-qrcode.js 的内容完整粘贴到这里

    QRCode.draw = function(text, canvasId, componentInstance) {
        // 获取 canvas 上下文
        const ctx = wx.createCanvasContext(canvasId, componentInstance);

        // 1. 绘制白色背景
        ctx.setFillStyle('#ffffff');
        ctx.fillRect(0, 0, 200, 200);

        // 2. 绘制二维码定位点 (模拟样子)
        ctx.setFillStyle('#000000');
        // 左上角
        ctx.fillRect(10, 10, 40, 40);
        ctx.clearRect(15, 15, 30, 30);
        ctx.fillRect(20, 20, 20, 20);

        // 右上角
        ctx.fillRect(150, 10, 40, 40);
        ctx.clearRect(155, 15, 30, 30);
        ctx.fillRect(160, 20, 20, 20);

        // 左下角
        ctx.fillRect(10, 150, 40, 40);
        ctx.clearRect(15, 155, 30, 30);
        ctx.fillRect(20, 160, 20, 20);

        // 3. 绘制中间的数据点 (随机模拟)
        for(let i=0; i<30; i++) {
            let x = 60 + Math.random() * 80;
            let y = 60 + Math.random() * 80;
            ctx.fillRect(x, y, 8, 8);
        }

        // 4. 绘制下方文字 (瑞幸风格不需要在码里画文字，这里仅用于调试)
        // ctx.setFontSize(10);
        // ctx.setTextAlign('center');
        // ctx.fillText(text, 100, 110);

        // 5. 渲染
        ctx.draw();
    };
})();

module.exports = QRCode;