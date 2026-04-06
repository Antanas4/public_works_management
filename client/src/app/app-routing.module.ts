import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {AuthGuard} from "./core/guards/auth.guard";
import {RoleGuard} from "./core/guards/role.guard";
import {DashboardRedirectGuard} from "./core/guards/dashboard-redirect.guard";
import {DummyRedirectComponent} from "./shared/components/dummy/dummy-redirect.component";

const routes: Routes = [
  {
    path: 'register',
    loadChildren: () => import('./features/register/register.module')
      .then(m => m.RegisterModule)
  },

  {
    path: 'login',
    loadChildren: () => import('./features/login/login.module')
      .then(m => m.LoginModule)
  },

  {
    path: '',
    canActivate: [DashboardRedirectGuard],
    component: DummyRedirectComponent
  },

  {
    path: 'admin',
    canActivate: [AuthGuard, RoleGuard],
    data: { role: 'ADMIN' },
    loadChildren: () =>
      import('./features/dashboard/pages/dashboard-admin/dashboard-admin.module')
        .then(m => m.DashboardAdminModule)
  },

  {
    path: 'dashboard',
    canActivate: [AuthGuard, RoleGuard],
    data: { role: 'CLIENT' },
    loadChildren: () =>
      import('./features/dashboard/pages/dashboard-client/dashboard-client.module')
        .then(m => m.DashboardClientModule)
  },

  {
    path: 'cases',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/create-case/create-case.module')
      .then(m => m.CreateCaseModule)
  },

  {
    path: 'cases/admin/:id',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/view-case/pages/view-case-admin/view-case-admin.module')
      .then(m => m.ViewCaseAdminModule)
  },

  {
    path: 'cases/:id',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/view-case/pages/view-case-client/view-case-client.module')
      .then(m => m.ViewCaseClientModule)
  },

  {
    path: 'suppliers',
    canActivate: [AuthGuard, RoleGuard],
    data: { role: 'ADMIN' },
    loadChildren: () =>
      import('./features/create-supplier/create-supplier.module')
        .then(m => m.CreateSupplierModule)
  },

  {
    path: '**',
    redirectTo: ''
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
