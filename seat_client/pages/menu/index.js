const app = getApp();
const request = require('../../utils/request.js'); // 假设 utils 中有 request 封装

Page({
  data: {
    // 配送类型：0-自取，1-外卖
    deliveryType: 0,
    
    // 分类数据
    categories: [],
    
    // 当前激活的分类索引
    activeCategoryIndex: 0,
    
    // 右侧滚动目标
    toView: '',
    
    // 购物车数据
    cartList: [],
    
    // 购物车总数量
    totalQty: 0,
    
    // 购物车总价
    totalPrice: 0,
    
    // 是否显示购物车详情
    showCartDetail: false
  },

  onLoad() {
    this.loadMenuData();
  },

  // 加载菜单数据
  loadMenuData() {
    wx.showLoading({ title: '加载中' });
    
    // 从服务器获取菜单数据
    request.get('/api/app/store/menu').then(res => {
      wx.hideLoading();
      if (res.code === 200) {
        // 将产品按分类分组
        const products = res.data || [];
        const categoriesMap = {};
        
        products.forEach(product => {
          const categoryId = product.categoryId || 0;
          if (!categoriesMap[categoryId]) {
            categoriesMap[categoryId] = {
              id: categoryId,
              name: product.categoryName || `分类${categoryId}`,
              count: 0,
              products: []
            };
          }
          
          categoriesMap[categoryId].products.push({
            id: product.id,
            name: product.name,
            description: product.description,
            price: product.price,
            originalPrice: product.originalPrice,
            imgUrl: product.imgUrl || "",
            cartQty: 0
          });
        });
        
        const categories = Object.values(categoriesMap);
        
        this.setData({
          categories: categories,
          toView: 'cat-0'
        });
      } else {
        // 如果接口失败，使用模拟数据
        this.loadMockData();
        wx.showToast({ title: '使用模拟数据', icon: 'none' });
      }
    }).catch(err => {
      wx.hideLoading();
      console.error('获取菜单失败:', err);
      // 如果接口失败，使用模拟数据
      this.loadMockData();
      wx.showToast({ title: '使用模拟数据', icon: 'none' });
    });
  },

  // 加载模拟数据
  loadMockData() {
    const mockCategories = [
      {
        id: 1,
        name: "本周热销",
        count: 0,
        products: [
          { id: 101, name: "Java并发编程实战", description: "深入理解Java线程核心", price: 59.90, originalPrice: 89.90, imgUrl: "", cartQty: 0 },
          { id: 102, name: "深入理解计算机系统", description: "CSAPP黑皮书", price: 128.00, imgUrl: "", cartQty: 0 },
          { id: 103, name: "算法导论", description: "经典算法教材", price: 98.00, originalPrice: 128.00, imgUrl: "", cartQty: 0 }
        ]
      },
      {
        id: 2,
        name: "文学小说",
        count: 0,
        products: [
          { id: 201, name: "三体全集", description: "刘慈欣科幻巨作", price: 68.50, imgUrl: "", cartQty: 0 },
          { id: 202, name: "百年孤独", description: "马尔克斯代表作", price: 45.00, imgUrl: "", cartQty: 0 },
          { id: 203, name: "挪威的森林", description: "村上春树经典", price: 38.00, imgUrl: "", cartQty: 0 }
        ]
      },
      {
        id: 3,
        name: "技术编程",
        count: 0,
        products: [
          { id: 301, name: "Python编程：从入门到实践", description: "Python入门经典", price: 69.00, imgUrl: "", cartQty: 0 },
          { id: 302, name: "Vue.js实战", description: "前端框架实战指南", price: 79.00, imgUrl: "", cartQty: 0 },
          { id: 303, name: "React Native实战", description: "移动端开发实战", price: 89.00, imgUrl: "", cartQty: 0 }
        ]
      },
      {
        id: 4,
        name: "人文历史",
        count: 0,
        products: [
          { id: 401, name: "明朝那些事儿", description: "当年明月历史著作", price: 168.00, imgUrl: "", cartQty: 0 },
          { id: 402, name: "人类简史", description: "尤瓦尔·赫拉利", price: 68.00, imgUrl: "", cartQty: 0 }
        ]
      }
    ];
    
    this.setData({
      categories: mockCategories,
      toView: 'cat-0'
    });
  },

  // 切换配送类型
  switchDelivery(e) {
    const type = parseInt(e.currentTarget.dataset.type);
    this.setData({ deliveryType: type });
  },

  // 点击左侧分类
  handleCategoryClick(e) {
    const index = e.currentTarget.dataset.index;
    const id = e.currentTarget.dataset.id;
    
    this.setData({
      activeCategoryIndex: index,
      toView: id
    });
  },

  // 右侧滚动监听
  onRightScroll(e) {
    // 简化处理：实际需要根据滚动位置计算当前分类
    // 这里暂时不实现复杂的联动逻辑
  },

  // 更新购物车
  updateCart(e) {
    const { item, action } = e.currentTarget.dataset;
    const { categories } = this.data;
    
    // 更新商品数量
    const newCategories = categories.map(cat => {
      const newProducts = cat.products.map(prod => {
        if (prod.id === item.id) {
          if (action === 'plus') {
            prod.cartQty = (prod.cartQty || 0) + 1;
          } else if (action === 'minus' && prod.cartQty > 0) {
            prod.cartQty--;
          }
        }
        return prod;
      });
      
      // 更新分类上的数量标记
      const count = newProducts.reduce((sum, prod) => sum + (prod.cartQty || 0), 0);
      return { ...cat, products: newProducts, count };
    });
    
    this.setData({ categories: newCategories });
    
    // 更新购物车数据
    this.updateCartData();
  },

  // 更新购物车数据
  updateCartData() {
    const { categories } = this.data;
    
    // 提取购物车商品
    const cartList = [];
    let totalQty = 0;
    let totalPrice = 0;
    
    categories.forEach(cat => {
      cat.products.forEach(prod => {
        if (prod.cartQty > 0) {
          cartList.push({
            id: prod.id,
            name: prod.name,
            price: prod.price,
            quantity: prod.cartQty
          });
          
          totalQty += prod.cartQty;
          totalPrice += prod.price * prod.cartQty;
        }
      });
    });
    
    this.setData({
      cartList,
      totalQty,
      totalPrice: totalPrice.toFixed(2)
    });
  },

  // 切换购物车详情显示
  toggleCartDetail() {
    this.setData({
      showCartDetail: !this.data.showCartDetail
    });
  },

  // 清空购物车
  clearCart() {
    const { categories } = this.data;
    
    // 重置所有商品数量
    const newCategories = categories.map(cat => {
      const newProducts = cat.products.map(prod => {
        prod.cartQty = 0;
        return prod;
      });
      return { ...cat, products: newProducts, count: 0 };
    });
    
    this.setData({
      categories: newCategories,
      cartList: [],
      totalQty: 0,
      totalPrice: '0.00',
      showCartDetail: false
    });
  },

  // 去结算
  goSettle() {
    if (this.data.totalQty === 0) {
      wx.showToast({
        title: '请先选择商品',
        icon: 'none'
      });
      return;
    }
    
    // 存储购物车数据到本地，供结算页使用
    wx.setStorageSync('cart_items', this.data.cartList);
    
    // 跳转到结算页面
    wx.navigateTo({
      url: '/pages/orders/checkout/checkout'
    });
  }
});