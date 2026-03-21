import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {AuthGuard} from "./core/guards/auth.guard";

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
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/dashboard/dashboard.module')
      .then(m => m.DashboardModule)
  },

  {
    path: 'cases',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/create-case/case.module')
      .then(m => m.CaseModule)
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
