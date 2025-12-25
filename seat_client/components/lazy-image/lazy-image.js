/**
 * 图片懒加载组件
 * 使用 IntersectionObserver API 实现
 */
Component({
  properties: {
    src: {
      type: String,
      value: ''
    },
    mode: {
      type: String,
      value: 'aspectFill'
    },
    placeholder: {
      type: String,
      value: '/assets/images/placeholder.png' // 占位图
    }
  },

  data: {
    loaded: false,
    showPlaceholder: true
  },

  lifetimes: {
    attached() {
      this.initObserver();
    },
    detached() {
      if (this.observer) {
        this.observer.disconnect();
      }
    }
  },

  methods: {
    initObserver() {
      // 创建 IntersectionObserver 实例
      this.observer = wx.createIntersectionObserver(this, {
        thresholds: [0.1] // 当图片进入视口10%时触发
      });

      this.observer.relativeToViewport().observe('.lazy-image-container', (res) => {
        if (res.intersectionRatio > 0 && !this.data.loaded) {
          // 图片进入视口，开始加载
          this.setData({ loaded: true });
          this.triggerEvent('load');
        }
      });
    },

    onImageLoad() {
      // 图片加载完成，隐藏占位图
      this.setData({ showPlaceholder: false });
      this.triggerEvent('loadsuccess');
    },

    onImageError() {
      // 图片加载失败
      this.setData({ showPlaceholder: true });
      this.triggerEvent('loaderror');
    }
  }
});

