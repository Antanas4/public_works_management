import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';

const routes: Routes = [
    {
        path: '',
        loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule)
    },
    {
        path: 'cases',
        loadChildren: () => import('./features/create-case/case.module').then(m => m.CaseModule)
    },
    {
        path: 'cases/:id',
        loadChildren: () => import('./features/view-case/view-case.module').then(m => m.ViewCaseModule)
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
