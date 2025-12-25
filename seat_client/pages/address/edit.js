const addressApi = require('../../api/address');

Page({
  data: {
    addressId: null,
    isEdit: false,
    name: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: false,
    latitude: null,
    longitude: null
  },

  onLoad(options) {
    if (options.id) {
      // 编辑模式
      this.setData({ addressId: options.id, isEdit: true });
      this.loadAddressDetail(options.id);
    }
  },

  // 加载地址详情
  loadAddressDetail(id) {
    addressApi.getAddressList()
      .then(res => {
        if (res.code === 200) {
          const address = res.data.find(addr => addr.id === parseInt(id));
          if (address) {
            this.setData({
              name: address.name || '',
              phone: address.phone || '',
              province: address.province || '',
              city: address.city || '',
              district: address.district || '',
              detail: address.detail || '',
              isDefault: address.isDefault || false,
              latitude: address.latitude,
              longitude: address.longitude
            });
          }
        }
      })
      .catch(err => {
        console.error('加载地址详情失败', err);
        wx.showToast({ title: '加载失败', icon: 'none' });
      });
  },

  // 输入处理
  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
  },

  // 切换默认地址
  toggleDefault() {
    this.setData({ isDefault: !this.data.isDefault });
  },

  // 选择位置（使用微信地图）
  chooseLocation() {
    wx.chooseLocation({
      success: (res) => {
        // 设置详细地址和经纬度
        this.setData({
          detail: res.address || res.name,
          latitude: res.latitude,
          longitude: res.longitude
        });

        // 尝试从地址字符串中解析省市区信息
        // 微信返回的 address 格式通常是：省市区详细地址
        const address = res.address || '';
        if (address) {
          // 简单的地址解析逻辑（可根据实际情况优化）
          // 格式示例：广东省深圳市南山区科技园南区
          const provinceMatch = address.match(/^(.+?省|.+?市|.+?自治区|.+?特别行政区)/);
          if (provinceMatch) {
            this.setData({ province: provinceMatch[1] });
            const cityMatch = address.substring(provinceMatch[0].length).match(/^(.+?市|.+?州|.+?盟)/);
            if (cityMatch) {
              this.setData({ city: cityMatch[1] });
              const districtMatch = address.substring(provinceMatch[0].length + cityMatch[0].length).match(/^(.+?区|.+?县|.+?市)/);
              if (districtMatch) {
                this.setData({ district: districtMatch[1] });
              }
            }
          }
        }

        wx.showToast({ 
          title: '定位成功', 
          icon: 'success',
          duration: 1500
        });
      },
      fail: (err) => {
        console.error('选择位置失败', err);
        if (err.errMsg && err.errMsg.includes('auth deny')) {
          wx.showModal({
            title: '需要位置权限',
            content: '请在小程序设置中开启位置权限',
            showCancel: false
          });
        } else {
          wx.showToast({ title: '选择位置失败', icon: 'none' });
        }
      }
    });
  },

  // 选择省市区（可以使用picker组件）
  chooseRegion() {
    // 这里可以使用picker-view或者第三方省市区选择组件
    // 简化处理，使用input让用户手动输入
    wx.showToast({ title: '请手动输入省市区', icon: 'none' });
  },

  // 保存地址
  saveAddress() {
    const { name, phone, province, city, district, detail, isDefault, latitude, longitude, addressId, isEdit } = this.data;

    // 简单校验
    if (!name || !phone || !detail) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' });
      return;
    }

    // 手机号格式校验
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }

    const addressData = {
      name,
      phone,
      province,
      city,
      district,
      detail,
      isDefault,
      latitude: latitude || null,
      longitude: longitude || null
    };

    wx.showLoading({ title: '保存中...' });

    if (isEdit) {
      // 更新地址
      addressApi.updateAddress(addressId, addressData)
        .then(res => {
          wx.hideLoading();
          if (res.code === 200) {
            wx.showToast({ title: '保存成功', icon: 'success' });
            setTimeout(() => {
              wx.navigateBack();
            }, 1500);
          }
        })
        .catch(err => {
          wx.hideLoading();
          console.error('保存地址失败', err);
          wx.showToast({ title: err.message || '保存失败', icon: 'none' });
        });
    } else {
      // 添加地址
      addressApi.addAddress(addressData)
        .then(res => {
          wx.hideLoading();
          if (res.code === 200) {
            wx.showToast({ title: '添加成功', icon: 'success' });
            setTimeout(() => {
              wx.navigateBack();
            }, 1500);
          }
        })
        .catch(err => {
          wx.hideLoading();
          console.error('添加地址失败', err);
          wx.showToast({ title: err.message || '添加失败', icon: 'none' });
        });
    }
  }
});

