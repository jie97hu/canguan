<template>
  <div class="auth-layout">
    <div class="auth-ambient auth-ambient-left"></div>
    <div class="auth-ambient auth-ambient-right"></div>
    <div class="auth-layout__inner">
      <section class="auth-layout__intro">
        <div class="auth-brand">
          <span class="auth-brand__mark">CG</span>
          <div>
            <div class="auth-brand__title">面馆多分店支出记账系统</div>
            <div class="auth-brand__subtitle">轻量化、多分店、强隔离的浏览器端记账台</div>
          </div>
        </div>

        <div class="auth-copy">
          <h1>把分店支出记录从纸笔和聊天记录，变成可查询、可统计、可追踪的统一账本。</h1>
          <p>
            面向老板与录入员的基础管理底座，先把登录态、权限、路由和数据契约打稳，再接业务页面。
          </p>
        </div>
      </section>

      <section class="auth-layout__panel">
        <router-view />
      </section>
    </div>
  </div>
</template>
