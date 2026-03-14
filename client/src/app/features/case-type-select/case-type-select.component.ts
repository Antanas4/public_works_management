import { Component } from '@angular/core';
import {Router} from "@angular/router";
import {CaseType} from "../../core/models/case-type.model";
import {CASE_TYPES} from "../../core/constants/case-types.constant";
import {MatButtonModule} from "@angular/material/button";
import {MatExpansionModule} from "@angular/material/expansion";

@Component({
  selector: 'app-case-type-select',
  imports: [MatExpansionModule, MatButtonModule],
  templateUrl: './case-type-select.component.html',
  styleUrl: './case-type-select.component.scss',
})
export class CaseTypeSelectComponent {
  problemTypes: CaseType[] = CASE_TYPES;

  constructor(private router: Router) {}

  selectSubtype(type: string, subtype: string) {

    this.router.navigate(['/cases/create'], {
      state: {
        type,
        subtype
      }
    });
  }
}
