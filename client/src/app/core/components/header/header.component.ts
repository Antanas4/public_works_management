import { Component, OnInit, HostListener } from '@angular/core';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss'],
    standalone: false
})
export class HeaderComponent implements OnInit {
  lastScrollTop: number = 0;
  isNavbarHidden: boolean = false;

  constructor() { }

  @HostListener('window:scroll')
  onWindowScroll() {
    const currentScroll = window.pageYOffset || document.documentElement.scrollTop;

    this.isNavbarHidden = currentScroll > this.lastScrollTop && currentScroll > 50;

    this.lastScrollTop = currentScroll <= 0 ? 0 : currentScroll;
  }

  ngOnInit() {
  }

}
