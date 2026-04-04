import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {CreateSupplierComponent} from "./create-supplier/create-supplier.component";

const routes: Routes = [
  {
    path: 'create',
    component: CreateSupplierComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SuppliersRoutingModule {}