// 使用带缓存的请求工具，降低延迟
const request = require('../utils/request-cached.js')

module.exports = {
    /**
     * 获取指定门店的菜单列表
     * @param {number} storeId 门店ID（暂时不使用，后端接口不需要）
     */
    getStoreMenu(storeId) {
        // 后端接口：GET /api/app/store/menu
        // 不传categoryId参数，让后端使用默认值
        return request.get('/app/store/menu');
    },

    /**
     * 获取商品详情 (预留)
     * @param {number} productId 商品ID
     */
    getProductDetail(productId) {
        return request.get('/app/product/detail', { id: productId });
    }
}