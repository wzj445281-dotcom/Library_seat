import request from '../utils/request'

export default {
    /**
     * 获取商品列表 (支持分页/搜索)
     */
    getProductList(params) {
        return request({
            url: '/api/app/product/list',
            method: 'GET',
            data: params
        })
    },

    /**
     * 获取商品详情
     */
    getProductDetail(id) {
        return request({
            url: `/api/app/product/${id}`,
            method: 'GET'
        })
    },

    /**
     * 获取推荐商品 (用于首页)
     */
    getRecommendProducts() {
        // 暂时复用列表接口，取前4个
        return request({
            url: '/api/app/product/list',
            method: 'GET',
            data: {
                current: 1,
                size: 4,
                recommend: true // 假设后端支持此参数，或直接取前几条
            }
        })
    }
}