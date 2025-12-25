const request = require('../utils/request.js')

module.exports = {
    /**
     * 获取指定门店的菜单列表
     * @param {number} storeId 门店ID
     */
    getStoreMenu(storeId) {
        return request.get('/app/product/menu', { storeId: storeId });
    },

    /**
     * 获取商品详情 (预留)
     * @param {number} productId 商品ID
     */
    getProductDetail(productId) {
        return request.get('/app/product/detail', { id: productId });
    }
}