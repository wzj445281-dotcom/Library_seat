const request = require('../utils/request.js');

module.exports = {
  /**
   * 获取地址列表
   */
  getAddressList() {
    return request.get('/api/app/address/list');
  },

  /**
   * 获取默认地址
   */
  getDefaultAddress() {
    return request.get('/api/app/address/default');
  },

  /**
   * 添加地址
   * @param {object} data 地址数据 {name, phone, province, city, district, detail, isDefault, latitude, longitude}
   */
  addAddress(data) {
    return request.post('/api/app/address/add', data);
  },

  /**
   * 更新地址
   * @param {number} id 地址ID
   * @param {object} data 地址数据
   */
  updateAddress(id, data) {
    return request.put(`/api/app/address/update/${id}`, data);
  },

  /**
   * 删除地址
   * @param {number} id 地址ID
   */
  deleteAddress(id) {
    return request.delete(`/api/app/address/delete/${id}`);
  },

  /**
   * 设置默认地址
   * @param {number} id 地址ID
   */
  setDefaultAddress(id) {
    return request.put(`/api/app/address/set-default/${id}`);
  }
};

