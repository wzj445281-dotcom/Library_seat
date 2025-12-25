// 使用带缓存的请求工具，降低延迟
const request = require('../utils/request-cached.js')

module.exports = {
    /**
     * 获取指定门店的菜单列表
     * @param {number} storeId 门店ID（暂时不使用，后端接口不需要）
     */
    getStoreMenu(storeId) {
        // 后端接口：GET /api/app/store/menu?categoryId=null
        return request.get('/app/store/menu', { categoryId: null });
    },

    /**
     * 获取商品详情 (预留)
     * @param {number} productId 商品ID
     */
    getProductDetail(productId) {
        return request.get('/app/product/detail', { id: productId });
    }
}