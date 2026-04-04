import {Component} from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import {SupplierService} from "../../../core/services/supplier/supplier.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {Router} from "@angular/router";
import {CASE_TYPES} from "../../../core/constants/case-types.constant";

@Component({
  selector: 'app-create-supplier',
  templateUrl: './create-supplier.component.html',
  styleUrl: './create-supplier.component.scss',
  standalone: false
})
export class CreateSupplierComponent {
  caseTypes = CASE_TYPES;
  flattenedSubtypes = this.caseTypes.flatMap(type => type.subtypes);

  form = this.fb.group({
    name: ['', Validators.required],
    vatNumber: ['', Validators.pattern(/^\d{9}$/)],
    registrationCode: ['', Validators.pattern(/^[A-Za-z]{2}\d{9}$/)],
    email: ['', Validators.email],
    handledCaseSubtypes: [[], Validators.required],
    description: [
      '',
      [
        Validators.required,
        Validators.maxLength(2000)
      ]
    ]
  });

  constructor(
    private fb: FormBuilder,
    private supplierService: SupplierService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {
  }

  submit() {
    if (this.form.invalid) return;

    const formValue = this.form.value;

    const payload = {
      name: formValue.name!,
      source: 'MANUAL',
      handledCaseSubtypes: formValue.handledCaseSubtypes!,
      metadata: {
        vatNumber: formValue.vatNumber || '',
        registrationCode: formValue.registrationCode || '',
        email: formValue.email || '',
        metadata: formValue.description
      }
    };

    this.supplierService.createSupplier(payload as any)
      .subscribe({
        next: () => {
          this.snackBar.open('Įmonė pridėta sėkmingai', 'Gerai', {
            duration: 2500
          });

          this.router.navigate(['/']);
        },
        error: err => {
          console.error(err);
          this.snackBar.open('Nepavyko pridėti įmonės', 'Uždaryti');
        }
      });
  }
}
