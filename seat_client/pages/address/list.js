const addressApi = require('../../api/address');

Page({
  data: {
    addressList: [],
    loading: true,
    selectedMode: false, // 是否为选择模式（从结算页跳转过来）
    selectedAddressId: null // 已选中的地址ID
  },

  onLoad(options) {
    // 如果是从结算页跳转过来，标记为选择模式
    if (options.select === 'true') {
      this.setData({ selectedMode: true });
    }
    this.loadAddressList();
  },

  onShow() {
    // 每次显示时刷新列表（可能从编辑页返回）
    this.loadAddressList();
  },

  // 加载地址列表
  loadAddressList() {
    this.setData({ loading: true });
    addressApi.getAddressList()
      .then(res => {
        if (res.code === 200) {
          this.setData({
            addressList: res.data || [],
            loading: false
          });
        }
      })
      .catch(err => {
        console.error('加载地址列表失败', err);
        wx.showToast({ title: '加载失败', icon: 'none' });
        this.setData({ loading: false });
      });
  },

  // 添加新地址
  addAddress() {
    wx.navigateTo({
      url: '/pages/address/edit'
    });
  },

  // 编辑地址
  editAddress(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/address/edit?id=${id}`
    });
  },

  // 删除地址
  deleteAddress(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个地址吗？',
      success: (res) => {
        if (res.confirm) {
          addressApi.deleteAddress(id)
            .then(result => {
              if (result.code === 200) {
                wx.showToast({ title: '删除成功', icon: 'success' });
                this.loadAddressList();
              }
            })
            .catch(err => {
              console.error('删除地址失败', err);
              wx.showToast({ title: '删除失败', icon: 'none' });
            });
        }
      }
    });
  },

  // 设置默认地址
  setDefault(e) {
    const id = e.currentTarget.dataset.id;
    addressApi.setDefaultAddress(id)
      .then(res => {
        if (res.code === 200) {
          wx.showToast({ title: '设置成功', icon: 'success' });
          this.loadAddressList();
        }
      })
      .catch(err => {
        console.error('设置默认地址失败', err);
        wx.showToast({ title: '设置失败', icon: 'none' });
      });
  },

  // 选择地址（选择模式下）
  selectAddress(e) {
    if (!this.data.selectedMode) return;

    const id = e.currentTarget.dataset.id;
    const address = this.data.addressList.find(addr => addr.id === id);
    
    if (address) {
      // 将选中的地址信息存储到全局，供结算页使用
      wx.setStorageSync('selectedAddress', address);
      
      // 返回上一页
      wx.navigateBack();
    }
  }
});

