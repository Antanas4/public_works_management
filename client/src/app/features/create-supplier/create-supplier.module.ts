import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {CreateSupplierComponent} from "./create-supplier/create-supplier.component";
import {ReactiveFormsModule} from "@angular/forms";
import {SuppliersRoutingModule} from "./create-supplier-routing.module";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatSelectModule} from "@angular/material/select";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatSnackBarModule} from "@angular/material/snack-bar";



@NgModule({
  declarations: [CreateSupplierComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SuppliersRoutingModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCardModule,
    MatSnackBarModule
  ]
})
export class CreateSupplierModule { }
