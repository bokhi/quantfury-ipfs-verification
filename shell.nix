{ pkgs ? import <nixpkgs> {} }:
pkgs.mkShell {
  buildInputs = [
    pkgs.p7zip
    pkgs.dos2unix
    pkgs.bc
    pkgs.babashka
    ];
}