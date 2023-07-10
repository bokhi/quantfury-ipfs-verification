{ pkgs ? import <nixpkgs> {} }:
pkgs.mkShell {
  buildInputs = [
    pkgs.jdk
    pkgs.clojure
    pkgs.p7zip
    pkgs.dos2unix
    pkgs.bc
    pkgs.babashka
    pkgs.ipget
    ];
}